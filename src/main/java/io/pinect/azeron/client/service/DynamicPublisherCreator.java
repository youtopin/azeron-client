package io.pinect.azeron.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.service.publisher.EventMessagePublisher;
import io.pinect.azeron.client.service.publisher.EventPublisher;
import nats.client.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class DynamicPublisherCreator {
    private final EventMessagePublisher eventMessagePublisher;
    private final ObjectMapper objectMapper;
    private final TaskExecutor publisherThreadExecutor;

    @Autowired
    public DynamicPublisherCreator(EventMessagePublisher eventMessagePublisher, ObjectMapper objectMapper, TaskExecutor publisherThreadExecutor) {
        this.eventMessagePublisher = eventMessagePublisher;
        this.objectMapper = objectMapper;
        this.publisherThreadExecutor = publisherThreadExecutor;
    }



    public class DynamicPublisher implements EventPublisher {

        @Override
        public void publish(Object o, MessageHandler messageHandler) {
            
        }

        @Override
        public void publish(Object o) {

        }
    }

}
