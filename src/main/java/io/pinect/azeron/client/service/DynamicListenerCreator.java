package io.pinect.azeron.client.service;

import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import io.pinect.azeron.client.domain.model.ClientConfig;
import io.pinect.azeron.client.service.listener.AbstractAzeronMessageHandler;
import io.pinect.azeron.client.service.listener.AzeronEventListener;
import io.pinect.azeron.client.service.listener.AzeronMessageHandlerDependencyHolder;
import io.pinect.azeron.client.service.listener.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Service
public class DynamicListenerCreator {
    private final EventListenerRegistry eventListenerRegistry;
    private final AzeronMessageHandlerDependencyHolder azeronMessageHandlerDependencyHolder;
    private final List<DynamicListener> dynamicListeners;
    private final String serviceName;

    @Autowired
    public DynamicListenerCreator(EventListenerRegistry eventListenerRegistry, AzeronMessageHandlerDependencyHolder azeronMessageHandlerDependencyHolder, @Value("${spring.application.name}") String serviceName) {
        this.eventListenerRegistry = eventListenerRegistry;
        this.azeronMessageHandlerDependencyHolder = azeronMessageHandlerDependencyHolder;
        this.serviceName = serviceName;
        dynamicListeners = new ArrayList<>();
    }

    public DynamicListenerBuilder getBuilderForClass(Class<?> aClass){
        return new DynamicListenerBuilder(aClass);
    }

    @PreDestroy
    public void clean(){
        dynamicListeners.forEach(DynamicListener::close);
    }

    public class DynamicListenerBuilder {
        private AzeronEventListener.AzeronMessageProcessor<?> processor;
        private AzeronEventListener.AzeronErrorHandler errorHandler;
        private final Class<?> aClass;
        private String channelName;
        private HandlerPolicy policy;

        public DynamicListenerBuilder(Class<?> aClass) {
            this.aClass = aClass;
        }

        public DynamicListenerBuilder setErrorHandler(AzeronEventListener.AzeronErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public DynamicListenerBuilder setProcessor(AzeronEventListener.AzeronMessageProcessor<?> processor) {
            this.processor = processor;
            return this;
        }

        public DynamicListenerBuilder setChannelName(String channelName) {
            this.channelName = channelName;
            return this;
        }

        public DynamicListenerBuilder setPolicy(HandlerPolicy policy) {
            this.policy = policy;
            return this;
        }

        public DynamicListener<?> build(){
            DynamicListener<?> dynamicListener = new DynamicListener<>(new AbstractAzeronMessageHandler<Object>(azeronMessageHandlerDependencyHolder) {
                @Override
                public AzeronMessageProcessor azeronMessageProcessor() {
                    return processor;
                }

                @Override
                public AzeronErrorHandler azeronErrorHandler() {
                    return errorHandler != null ? errorHandler : new AzeronErrorHandler() {
                        @Override
                        public void onError(Exception e, MessageDto messageDto) {

                        }
                    };
                }

                @Override
                public String serviceName() {
                    return serviceName;
                }

                @Override
                public HandlerPolicy policy() {
                    return policy != null ? policy : HandlerPolicy.FULL;
                }

                @Override
                public Class eClass() {
                    return aClass;
                }

                @Override
                public ClientConfig clientConfig() {
                    return new ClientConfig(1, serviceName, false);
                }

                @Override
                public String eventName() {
                    return channelName;
                }
            });
            dynamicListeners.add(dynamicListener);

            return dynamicListener;
        }
    }

    public class DynamicListener<E> {
        private final EventListener<E> eventListener;

        private DynamicListener(EventListener<E> eventListener) {
            this.eventListener = eventListener;
            eventListenerRegistry.register(eventListener);
        }

        public void close(){
            eventListenerRegistry.drop(eventListener.eventName());
        }

    }

}
