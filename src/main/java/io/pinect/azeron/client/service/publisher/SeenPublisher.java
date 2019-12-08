package io.pinect.azeron.client.service.publisher;

public interface SeenPublisher {
    void publishSeen(String messageId) throws Exception;
    void publishSeen(String messageId, String channelName) throws Exception;
}
