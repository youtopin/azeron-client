package io.pinect.azeron.client.service.handler;

import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import io.pinect.azeron.client.domain.model.ClientConfig;
import nats.client.MessageHandler;
import org.springframework.lang.Nullable;

public interface EventListener<E> extends SimpleEventListener {
    HandlerPolicy policy();
    Class<E> eClass();
    ClientConfig clientConfig();

    default boolean useAzeron(){
        return !policy().equals(HandlerPolicy.NO_AZERON);
    }

    void handle(String messageBody);

    void handle(MessageDto messageDto);
}
