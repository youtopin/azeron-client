package io.pinect.azeron.client.service.publisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import io.pinect.azeron.client.domain.repository.FallbackRepository;
import io.pinect.azeron.client.exception.PublishException;
import io.pinect.azeron.client.service.AzeronServerStatusTracker;
import io.pinect.azeron.client.service.NatsReconnectForceService;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import nats.client.MessageHandler;
import nats.client.Nats;
import org.springframework.lang.Nullable;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Log4j2
public class EventMessagePublisher {
    private final AtomicReference<Nats> natsAtomicReference;
    private final ObjectMapper objectMapper;
    private final AzeronServerStatusTracker azeronServerStatusTracker;
    private final RetryTemplate eventPublishRetryTemplate;
    private final FallbackRepository fallbackRepository;
    private final String serviceName;


    public EventMessagePublisher(AtomicNatsHolder atomicNatsHolder, ObjectMapper objectMapper, AzeronServerStatusTracker azeronServerStatusTracker, FallbackRepository fallbackRepository, RetryTemplate eventPublishRetryTemplate, String serviceName) {
        this.natsAtomicReference = atomicNatsHolder.getNatsAtomicReference();
        this.objectMapper = objectMapper;
        this.azeronServerStatusTracker = azeronServerStatusTracker;
        this.eventPublishRetryTemplate = eventPublishRetryTemplate;
        this.fallbackRepository = fallbackRepository;
        this.serviceName = serviceName;
    }

    public void sendMessage(String eventName, String message, PublishStrategy publishStrategy) throws Exception {
        sendMessage(eventName, message, publishStrategy, null, false);
    }

    public void sendRawMessage(String eventName, String message, PublishStrategy publishStrategy) throws Exception {
        sendMessage(eventName, message, publishStrategy, null, true);
    }

    void sendMessage(String eventName, String message, PublishStrategy publishStrategy, @Nullable MessageHandler messageHandler) throws Exception {
        sendMessage(eventName, message, publishStrategy, messageHandler, false);
    }

    void sendMessage(String eventName, String message, PublishStrategy publishStrategy, @Nullable MessageHandler messageHandler, boolean isRaw) throws Exception {
        switch (publishStrategy){
            case AZERON_NO_FALLBACK:
                sendAzeronMessage(eventName, message, false, messageHandler, isRaw);
                break;
            case BLOCKED:
                sendMessageBlocked(eventName, message, messageHandler, isRaw);
                break;
            case NATS:
                sendNatsMessage(eventName, message, true, messageHandler, isRaw);
                break;
            case AZERON:
                sendAzeronMessage(eventName, message, true, messageHandler, isRaw);
                break;
        }
    }


    private void sendMessageBlocked(String eventName, String message, @Nullable MessageHandler messageHandler, boolean isRaw) throws Exception {
        eventPublishRetryTemplate.execute(new RetryCallback<Void, Exception>() {
            @Override
            public Void doWithRetry(RetryContext retryContext) throws Exception {
                sendAzeronMessage(eventName, message, false, messageHandler, isRaw);
                return null;
            }
        });
    }

    private void sendAzeronMessage(String eventName, String message, boolean fallback, @Nullable MessageHandler messageHandler, boolean isRaw) throws IOException, PublishException {
        publish(eventName, message, true, fallback, PublishStrategy.AZERON, messageHandler, isRaw);
    }

    private void sendNatsMessage(String eventName, String message, boolean fallback, @Nullable MessageHandler messageHandler, boolean isRaw) throws IOException, PublishException {
        publish(eventName, message, false, fallback, PublishStrategy.NATS, messageHandler, isRaw);
    }

    private void publish(String eventName, String message, boolean azeron, boolean fallback, PublishStrategy publishStrategy, @Nullable MessageHandler messageHandler, boolean isRaw) throws IOException, PublishException {
        String json;
        MessageDto messageDto = null;
        if(!isRaw){
            messageDto = getMessageDto(serviceName, eventName, message);
            json = getJson(messageDto);
        }else{
            json = message;
        }
        Nats nats = natsAtomicReference.get();
        if(!azeron || azeronServerStatusTracker.isUp()){
            if(nats.isConnected()) {
                if(messageHandler == null){
                    log.trace("Publishing to " + eventName);
                    nats.publish(eventName, json);

                } else {
                    log.trace("Sending request message to "+ eventName);
                    nats.request(eventName, json, 10, TimeUnit.SECONDS, messageHandler);
                }
                return;
            }
        }

        if(fallback && !isRaw)
            handleFallback(messageDto, eventName, publishStrategy);

        throw new PublishException(serviceName, message);
    }

    private void handleFallback(MessageDto messageDto, String eventName, PublishStrategy publishStrategy) {
        fallbackRepository.saveMessage(new FallbackRepository.FallbackEntity(messageDto.getMessageId(), messageDto.getObject().toString(), eventName, publishStrategy));
    }

    private MessageDto getMessageDto(String deviceName, String eventName, String message) throws IOException {
        MessageDto messageDto = new MessageDto();
        messageDto.setChannelName(eventName);
        messageDto.setMessageId(UUID.randomUUID().toString());
        messageDto.setServiceName(serviceName);
        JsonNode jsonNodeMessage = objectMapper.readValue(message, JsonNode.class);
        messageDto.setObject(jsonNodeMessage);
        messageDto.setTimeStamp(new Date().getTime());
        return messageDto;
    }

    private String getJson(MessageDto messageDto) throws IOException {
        return objectMapper.writeValueAsString(messageDto);
    }

    public enum PublishStrategy {
        BLOCKED, AZERON, NATS, AZERON_NO_FALLBACK
    }
}
