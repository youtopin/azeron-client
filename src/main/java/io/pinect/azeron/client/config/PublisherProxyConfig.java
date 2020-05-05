package io.pinect.azeron.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.service.publisher.EventMessagePublisher;
import io.pinect.azeron.client.service.publisher.PublisherProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class PublisherProxyConfig {
    private final EventMessagePublisher eventMessagePublisher;
    private final ObjectMapper objectMapper;
    private final TaskExecutor publisherThreadExecutor;

    @Autowired
    public PublisherProxyConfig(EventMessagePublisher eventMessagePublisher, ObjectMapper objectMapper, TaskExecutor publisherThreadExecutor) {
        this.eventMessagePublisher = eventMessagePublisher;
        this.objectMapper = objectMapper;
        this.publisherThreadExecutor = publisherThreadExecutor;
    }

    @Bean
    public PublisherProxy publisherProxy() {
        return new PublisherProxy(eventMessagePublisher, objectMapper, publisherThreadExecutor);
    }

    @Bean(name = "publisherProxyBeanFactory")
    public PublisherProxyBeanFactory webServiceProxyBeanFactory() {
        return new PublisherProxyBeanFactory();
    }
}
