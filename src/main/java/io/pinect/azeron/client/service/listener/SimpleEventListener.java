package io.pinect.azeron.client.service.listener;

import io.pinect.azeron.client.domain.dto.out.MessageDto;
import nats.client.Message;
import nats.client.MessageHandler;
import org.springframework.lang.Nullable;

public interface SimpleEventListener<E> extends MessageHandler {
    AzeronMessageProcessor<E> azeronMessageProcessor();
    AzeronErrorHandler azeronErrorHandler();
    String serviceName();

    interface AzeronErrorHandler {
        void onError(Exception e, @Nullable MessageDto messageDto);
    }

    interface AzeronMessageProcessor<E> {
        void process(E e);
    }

    @Override
    default void onMessage(Message message) {

    }
}
