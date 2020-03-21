package io.pinect.azeron.client.service.publisher;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
public @interface Publisher {
    Class forClass();
    boolean raw() default false;
    EventMessagePublisher.PublishStrategy publishStrategy() default EventMessagePublisher.PublishStrategy.AZERON_NO_FALLBACK;
    String eventName();
}
