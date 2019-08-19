package io.pinect.azeron.client.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.service.lock.HandlingLock;
import io.pinect.azeron.client.service.publisher.SeenPublisher;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
@Getter
public class AzeronMessageHandlerDependencyHolder {
    private final ObjectMapper objectMapper;
    private final SeenPublisher seenPublisher;
    private final Executor seenExecutor;
    private final HandlingLock handlingLock;

    @Autowired
    public AzeronMessageHandlerDependencyHolder(ObjectMapper objectMapper, SeenPublisher seenPublisher, Executor seenExecutor, HandlingLock handlingLock) {
        this.objectMapper = objectMapper;
        this.seenPublisher = seenPublisher;
        this.seenExecutor = seenExecutor;
        this.handlingLock = handlingLock;
    }
}