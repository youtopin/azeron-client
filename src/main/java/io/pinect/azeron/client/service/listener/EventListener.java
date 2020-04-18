package io.pinect.azeron.client.service.listener;

import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import io.pinect.azeron.client.domain.model.ClientConfig;

public interface EventListener<E> extends AzeronEventListener {
    HandlerPolicy policy();
    Class<E> eClass();
    ClientConfig clientConfig();
    String eventName();

    default boolean useAzeron(){
        return !policy().equals(HandlerPolicy.NO_AZERON);
    }

    void handle(String messageBody);

    void handle(MessageDto messageDto);
}
