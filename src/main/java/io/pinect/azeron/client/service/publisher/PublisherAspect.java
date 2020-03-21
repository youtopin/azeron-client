package io.pinect.azeron.client.service.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.config.properties.AzeronClientProperties;
import io.pinect.azeron.client.domain.repository.FallbackRepository;
import io.pinect.azeron.client.service.AzeronServerStatusTracker;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import nats.client.MessageHandler;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Log4j2
public class PublisherAspect {
    private final EventMessagePublisher eventMessagePublisher;
    private final ObjectMapper objectMapper;

    @Autowired
    public PublisherAspect(AtomicNatsHolder atomicNatsHolder, ObjectMapper objectMapper, AzeronServerStatusTracker azeronServerStatusTracker, RetryTemplate eventPublishRetryTemplate, FallbackRepository fallbackRepository, @Value("${spring.application.name}") String serviceName, AzeronClientProperties azeronClientProperties) {
        this.eventMessagePublisher = new EventMessagePublisher(atomicNatsHolder, objectMapper, azeronServerStatusTracker, fallbackRepository, eventPublishRetryTemplate, serviceName, azeronClientProperties.getNatsRequestTimeoutSeconds());
        this.objectMapper = objectMapper;
    }

    /*@Pointcut(value = "execution(* publish(..)) && args(object, messageHandler) && (@annotation(publisher) || @within(publisher))", argNames = "object,messageHandler,publisher")
    public void publishPointcut(final Object object, @Nullable final MessageHandler messageHandler, final Publisher publisher){}

    @After(value = "publishPointcut(object, messageHandler, publisher)", argNames = "object, messageHandler, publisher")
    public void publishByMethod(final Object object, @Nullable final MessageHandler messageHandler, final Publisher publisher) {
        doPublish(object, messageHandler, publisher);
    }*/

    @After(value = "execution(* io.pinect.azeron.client.service.publisher.EventPublisher.publish(..)) && args(object, messageHandler) && @within(publisher)", argNames = "object,messageHandler,publisher")
    public void publishByClass(final Object object, @Nullable final MessageHandler messageHandler, final Publisher publisher) {
        doPublish(object, messageHandler, publisher);
    }

    @SneakyThrows
    private void doPublish(Object param, MessageHandler messageHandler, Publisher publisher){
        String messageBody = objectMapper.writeValueAsString(publisher.forClass().cast(param));
        log.debug("Publishing message to event " + publisher.eventName() + " => " + messageBody + "");
        eventMessagePublisher.sendMessage(publisher.eventName(), messageBody, publisher.publishStrategy(), messageHandler, publisher.raw());
    }

}
