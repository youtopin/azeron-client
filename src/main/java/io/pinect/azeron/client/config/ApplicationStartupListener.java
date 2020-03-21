package io.pinect.azeron.client.config;

import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.model.ClientConfig;
import io.pinect.azeron.client.service.EventListenerRegistry;
import io.pinect.azeron.client.service.handler.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Log4j2
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {
    private final EventListenerRegistry eventListenerRegistry;
    private final AzeronMessageHandlerDependencyHolder azeronMessageHandlerDependencyHolder;
    private final ApplicationContext applicationContext;

    @Autowired
    public ApplicationStartupListener(EventListenerRegistry eventListenerRegistry, AzeronMessageHandlerDependencyHolder azeronMessageHandlerDependencyHolder, ApplicationContext applicationContext) {
        this.eventListenerRegistry = eventListenerRegistry;
        this.azeronMessageHandlerDependencyHolder = azeronMessageHandlerDependencyHolder;
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(AzeronListener.class);
        beansWithAnnotation.keySet().forEach(beanName -> {
            Object bean = beansWithAnnotation.get(beanName);
            AzeronListener azeronListener = applicationContext.findAnnotationOnBean(beanName, AzeronListener.class);
            if(bean instanceof SimpleEventListener){
                eventListenerRegistry.register(getEventListener((SimpleEventListener) bean, azeronListener));
            }else{
                throw new RuntimeException("");
            }
        });
    }

    private EventListener<?> getEventListener(SimpleEventListener<?> simpleEventListener, AzeronListener azeronListener){
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
                return simpleEventListener.azeronMessageProcessor();
            }

            @Override
            public AzeronErrorHandler azeronErrorHandler() {
                return simpleEventListener.azeronErrorHandler();
            }

            @Override
            public String serviceName() {
                return simpleEventListener.serviceName();
            }

            @Override
            public String eventName() {
                return azeronListener.eventName();
            }

            @Override
            public ClientConfig clientConfig() {
                return new ClientConfig(1, simpleEventListener.serviceName(), azeronListener.useQueueGroup());
            }
        };
    }
}
