package io.pinect.azeron.client.service.publisher;

import nats.client.MessageHandler;
import org.springframework.lang.Nullable;

public interface EventPublisher<E> {
    void publish(E e, @Nullable MessageHandler messageHandler);
}
