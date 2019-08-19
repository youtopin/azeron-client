package io.pinect.azeron.client.service.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.config.ChannelName;
import io.pinect.azeron.client.domain.dto.ResponseStatus;
import io.pinect.azeron.client.domain.dto.in.SeenResponseDto;
import io.pinect.azeron.client.domain.dto.out.SeenDto;
import lombok.extern.log4j.Log4j2;
import nats.client.Message;
import nats.client.MessageHandler;
import nats.client.Nats;
import nats.client.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Log4j2
public class AzeronSeenPublisher implements SeenPublisher {
    private final String serviceName;
    private final AtomicNatsHolder atomicNatsHolder;
    private final ObjectMapper objectMapper;

    @Autowired
    public AzeronSeenPublisher(AtomicNatsHolder atomicNatsHolder, ObjectMapper objectMapper, @Value("${spring.application.name}") String serviceName) {
        this.serviceName = serviceName;
        this.atomicNatsHolder = atomicNatsHolder;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishSeen(String messageId) throws Exception {
        String reqId = UUID.randomUUID().toString();
        SeenDto seenDto = SeenDto.builder().messageId(messageId).serviceName(serviceName).reqId(reqId).build();
        String value = null;

        try {
            value = objectMapper.writeValueAsString(seenDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        AtomicBoolean sent = new AtomicBoolean(false);
        Nats nats = atomicNatsHolder.getNatsAtomicReference().get();

        AtomicBoolean shouldWait = new AtomicBoolean(true);

        long timeNow = new Date().getTime();
        Request request = nats.request(ChannelName.AZERON_SEEN_CHANNEL_NAME, value, 5, TimeUnit.SECONDS, (MessageHandler) message -> {
            try {
                SeenResponseDto seenResponseDto = objectMapper.readValue(message.getBody(), SeenResponseDto.class);
                if (seenResponseDto.getStatus().equals(ResponseStatus.OK)){
                    sent.set(true);
                }
            } catch (IOException ignored) {}
            shouldWait.set(false);
        });

        while (shouldWait.get() && (new Date().getTime() - timeNow < 5000)){
            //wait
        }

        if(!sent.get())
            throw new Exception("Could not perform seen action");
    }
}
