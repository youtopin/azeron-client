package io.pinect.azeron.client.config;

import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.model.ClientConfig;
import io.pinect.azeron.client.service.EventListenerRegistry;
import io.pinect.azeron.client.service.listener.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
@Log4j2
public class AzeronApplicationStartupListener implements ApplicationListener<ApplicationStartedEvent> {
    private final EventListenerRegistry eventListenerRegistry;
    private final AzeronMessageHandlerDependencyHolder azeronMessageHandlerDependencyHolder;
    private final ApplicationContext applicationContext;

    @Autowired
    public AzeronApplicationStartupListener(EventListenerRegistry eventListenerRegistry, AzeronMessageHandlerDependencyHolder azeronMessageHandlerDependencyHolder, ApplicationContext applicationContext) {
        this.eventListenerRegistry = eventListenerRegistry;
        this.azeronMessageHandlerDependencyHolder = azeronMessageHandlerDependencyHolder;
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(AzeronListener.class);
        beansWithAnnotation.keySet().forEach(beanName -> {
            Object bean = beansWithAnnotation.get(beanName);
            if(bean instanceof EventListener){

            } else if(bean instanceof AzeronEventListener){
                AzeronListener azeronListener = applicationContext.findAnnotationOnBean(beanName, AzeronListener.class);
                eventListenerRegistry.register(getEventListener((AzeronEventListener) bean, azeronListener));
            } else {
                throw new RuntimeException("");
            }
        });

        Collection<EventListener> eventListeners = applicationContext.getBeansOfType(EventListener.class).values();
        eventListeners.forEach(eventListenerRegistry::register);
    }

    private EventListener<?> getEventListener(AzeronEventListener<?> azeronEventListener, AzeronListener azeronListener){
        return new AbstractAzeronMessageHandler(azeronMessageHandlerDependencyHolder){

            @Override
            public HandlerPolicy policy() {
                return azeronListener.policy();
            }

            @Override
            public Class eClass() {
                return azeronListener.ofClass();
            }

            @Override
            public AzeronMessageProcessor azeronMessageProcessor() {
                return azeronEventListener.azeronMessageProcessor();
            }

            @Override
            public AzeronErrorHandler azeronErrorHandler() {
                return azeronEventListener.azeronErrorHandler();
            }

            @Override
            public String serviceName() {
                return azeronEventListener.serviceName();
            }

            @Override
            public String eventName() {
                return azeronListener.eventName();
            }

            @Override
            public ClientConfig clientConfig() {
                return new ClientConfig(1, azeronEventListener.serviceName(), azeronListener.useQueueGroup());
            }
        };
    }
}
