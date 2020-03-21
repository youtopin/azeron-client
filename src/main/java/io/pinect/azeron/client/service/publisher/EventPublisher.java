package io.pinect.azeron.client.service.publisher;

import jdk.internal.jline.internal.Nullable;
import nats.client.MessageHandler;

public interface EventPublisher<E> {
    default void publish(E e, @Nullable MessageHandler messageHandler){}
}
