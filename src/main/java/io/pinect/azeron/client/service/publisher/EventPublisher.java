package io.pinect.azeron.client.service.publisher;

import nats.client.MessageHandler;

public interface EventPublisher<E> {
    void publish(E e, MessageHandler messageHandler);
    void publish(E e);
}
