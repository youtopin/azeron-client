package io.pinect.azeron.client.service.handler;

import io.pinect.azeron.client.domain.dto.out.MessageDto;
import nats.client.MessageHandler;
import org.springframework.lang.Nullable;

public interface SimpleEventListener<E> extends MessageHandler {
    AzeronMessageProcessor<E> azeronMessageProcessor();
    AzeronErrorHandler azeronErrorHandler();
    String serviceName();
    String eventName();

    interface AzeronErrorHandler {
        void onError(Exception e, @Nullable MessageDto messageDto);
    }

    interface AzeronMessageProcessor<E> {
        void process(E e);
    }
}
