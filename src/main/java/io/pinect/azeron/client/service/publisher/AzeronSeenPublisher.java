package io.pinect.azeron.client.service.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.config.ChannelName;
import io.pinect.azeron.client.domain.dto.ResponseStatus;
import io.pinect.azeron.client.domain.dto.in.SeenResponseDto;
import io.pinect.azeron.client.domain.dto.out.SeenDto;
import io.pinect.azeron.client.domain.repository.FallbackRepository;
import io.pinect.azeron.client.service.AzeronServerStatusTracker;
import nats.client.Message;
import nats.client.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AzeronSeenPublisher extends EventMessagePublisher implements SeenPublisher {
    private final String serviceName;

    @Autowired
    public AzeronSeenPublisher(AtomicNatsHolder atomicNatsHolder, ObjectMapper objectMapper, AzeronServerStatusTracker azeronServerStatusTracker, FallbackRepository fallbackRepository, RetryTemplate eventPublishRetryTemplate, @Value("${application.name}") String serviceName) {
        super(atomicNatsHolder, objectMapper, azeronServerStatusTracker, fallbackRepository, eventPublishRetryTemplate, serviceName);
        this.serviceName = serviceName;
    }

    @Override
    public void publishSeen(String messageId) throws Exception {
        String reqId = UUID.randomUUID().toString();
        SeenDto seenDto = SeenDto.builder().messageId(messageId).serviceName(serviceName).reqId(reqId).build();
        String value = null;
        try {
            value = getObjectMapper().writeValueAsString(seenDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        AtomicBoolean sent = new AtomicBoolean(false);
        sendMessage(ChannelName.AZERON_SEEN_CHANNEL_NAME, value, PublishStrategy.AZERON, new MessageHandler() {
            @Override
            public void onMessage(Message message) {
                try {
                    SeenResponseDto seenResponseDto = getObjectMapper().readValue(message.getBody(), SeenResponseDto.class);
                    if(seenResponseDto.getStatus().equals(ResponseStatus.OK) && seenResponseDto.getReqId().equals(reqId))
                        sent.set(true);
                } catch (IOException ignored) {}
            }
        });

        if(!sent.get())
            throw new Exception("Could not perform seen action");
    }
}
