package io.pinect.azeron.client.service.publisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import io.pinect.azeron.client.domain.repository.FallbackRepository;
import io.pinect.azeron.client.exception.PublishException;
import io.pinect.azeron.client.service.AzeronServerStatusTracker;
import lombok.Getter;
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


    public void sendMessage(String eventName, String message, PublishStrategy publishStrategy, @Nullable MessageHandler messageHandler) throws Exception {
        switch (publishStrategy){
            case BLOCKED:
                sendMessageBlocked(eventName, message, messageHandler);
                break;
            case NATS:
                sendNatsMessage(eventName, message, true, messageHandler);
                break;
            case AZERON:
                sendAzeronMessage(eventName, message, true, messageHandler);
                break;
        }
    }


    private void sendMessageBlocked(String eventName, String message, @Nullable MessageHandler messageHandler) throws Exception {
        eventPublishRetryTemplate.execute(new RetryCallback<Void, Exception>() {
            @Override
            public Void doWithRetry(RetryContext retryContext) throws Exception {
                sendAzeronMessage(eventName, message, false, messageHandler);
                return null;
            }
        });
    }

    private void sendAzeronMessage(String eventName, String message, boolean fallback, @Nullable MessageHandler messageHandler) throws IOException, PublishException {
        if(azeronServerStatusTracker.isUp()){
            publish(eventName, message, fallback, PublishStrategy.AZERON, messageHandler);
        }else {
            throw new PublishException(serviceName, message);
        }
    }

    private void sendNatsMessage(String eventName, String message, boolean fallback, @Nullable MessageHandler messageHandler) throws IOException, PublishException {
        publish(eventName, message, fallback, PublishStrategy.NATS, messageHandler);
    }

    private void publish(String eventName, String message, boolean fallback, PublishStrategy publishStrategy, @Nullable MessageHandler messageHandler) throws IOException, PublishException {
        MessageDto messageDto = getMessageDto(serviceName, eventName, message);
        String json = getJson(messageDto);
        Nats nats = natsAtomicReference.get();
        if(nats.isConnected()) {
            if(messageHandler == null)
                nats.publish(eventName, json);
            else
                nats.request(eventName, json, 20, TimeUnit.SECONDS, messageHandler);
            return;
        }

        if(fallback)
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
        BLOCKED, AZERON, NATS
    }
}
