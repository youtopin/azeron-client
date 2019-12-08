package io.pinect.azeron.client.service.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.config.ChannelName;
import io.pinect.azeron.client.config.properties.AzeronClientProperties;
import io.pinect.azeron.client.domain.dto.ResponseStatus;
import io.pinect.azeron.client.domain.dto.in.SeenResponseDto;
import io.pinect.azeron.client.domain.dto.out.SeenDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import nats.client.MessageHandler;
import nats.client.Nats;
import nats.client.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Log4j2
public class AzeronSeenPublisher implements SeenPublisher {
    private final String serviceName;
    private final AtomicNatsHolder atomicNatsHolder;
    private final ObjectMapper objectMapper;
    private final Semaphore semaphore;
    private final Queue<SeenRequest> queue = new ConcurrentLinkedQueue<SeenRequest>();

    @Autowired
    public AzeronSeenPublisher(AtomicNatsHolder atomicNatsHolder, ObjectMapper objectMapper, @Value("${spring.application.name}") String serviceName, AzeronClientProperties azeronClientProperties) {
        this.serviceName = serviceName;
        this.atomicNatsHolder = atomicNatsHolder;
        this.objectMapper = objectMapper;
        semaphore = new Semaphore(azeronClientProperties.getSeenPublishSemaphoreSize());
    }

    @Override
    public void publishSeen(String messageId) {
        publishSeen(messageId, "unknown");
    }

    @Override
    public void publishSeen(String messageId, String channelName) {
        boolean acquired = semaphore.tryAcquire();
        boolean seenSent = false;
        try {
            if(acquired){
                doSeen(messageId, channelName);
                seenSent = true;
                clearQueue();
            } else {
                queue.add(SeenRequest.builder().channelName(channelName).messageId(messageId).build());
            }
        } catch (Exception e) {
            log.catching(e);
            if(!seenSent)
                queue.add(SeenRequest.builder().channelName(channelName).messageId(messageId).build());
        } finally {
            if(acquired) {
                semaphore.release();
            }
        }
    }

    private void doSeen(String messageId, String channelName) throws Exception {
        String reqId = UUID.randomUUID().toString();
        SeenDto seenDto = SeenDto.builder().channelName(channelName).messageIds(Collections.singleton(messageId)).serviceName(serviceName).reqId(reqId).build();
        sendSeenMessage(seenDto);
    }

    private void sendSeenMessage(SeenDto seenDto) throws Exception {
        String value = null;
        try {
            value = objectMapper.writeValueAsString(seenDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        AtomicBoolean sent = new AtomicBoolean(false);
        AtomicBoolean shouldWait = new AtomicBoolean(true);
        Request request = sendRequest(value, shouldWait, sent);
        wait(shouldWait);
        request.close();
        if(!sent.get())
            throw new Exception("Could not perform seen action");
    }

    private Request sendRequest(String value, AtomicBoolean shouldWait, AtomicBoolean sent) {
        Nats nats = atomicNatsHolder.getNatsAtomicReference().get();
        return nats.request(ChannelName.AZERON_SEEN_CHANNEL_NAME, value, 5, TimeUnit.SECONDS, (MessageHandler) message -> {
            try {
                SeenResponseDto seenResponseDto = objectMapper.readValue(message.getBody(), SeenResponseDto.class);
                if (seenResponseDto.getStatus().equals(ResponseStatus.OK)){
                    sent.set(true);
                }
            } catch (IOException ignored) {}
            shouldWait.set(false);
        });
    }

    private void clearQueue() {
        Map<String, Set<String>> stringSetMap = dequeueIntoMap();
        for (String key : stringSetMap.keySet()) {
            String reqId = UUID.randomUUID().toString();
            String value = null;
            String finalChannelName = key.equals("unknown") ? null : key;
            Set<String> messageIds = stringSetMap.get(key);

            SeenDto seenDto = SeenDto.builder().messageIds(messageIds).serviceName(serviceName).reqId(reqId).build();
            try {
                value = objectMapper.writeValueAsString(seenDto);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            AtomicBoolean sent = new AtomicBoolean(false);
            Nats nats = atomicNatsHolder.getNatsAtomicReference().get();

            AtomicBoolean shouldWait = new AtomicBoolean(true);

            Request request = sendRequest(value, shouldWait, sent);
            wait(shouldWait);
            request.close();
            if(!sent.get()){
                messageIds.forEach(s -> {
                    queue.add(SeenRequest.builder().channelName(finalChannelName).messageId(s).build());
                });
            }

        }
    }

    private void wait(AtomicBoolean shouldWait) {
        long timeNow = new Date().getTime();
        while (shouldWait.get() && (new Date().getTime() - timeNow < 5100)){
            //wait
        }
    }

    @Builder
    @Getter
    @Setter
    private static class SeenRequest {
        private String messageId;
        private String channelName;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SeenRequest that = (SeenRequest) o;
            return Objects.equals(messageId, that.messageId);
        }

        @Override
        public int hashCode() {

            return Objects.hash(messageId);
        }
    }

    private Map<String, Set<String>> dequeueIntoMap(){
        int i = 0;
        Map<String, Set<String>> result = new HashMap<>();

        SeenRequest seenRequest = null;
        while ((seenRequest = queue.poll()) != null && i <= 99){
            String channelName = seenRequest.getChannelName() != null ? seenRequest.getChannelName() : "unknown";

            if(result.containsKey(channelName)){
                result.get(channelName).add(seenRequest.getMessageId());
            }else{
                Set<String> messageIds = new HashSet<>();
                messageIds.add(seenRequest.getMessageId());
                result.put(channelName, messageIds);
            }

            i++;
        }
        return result;
    }
}
