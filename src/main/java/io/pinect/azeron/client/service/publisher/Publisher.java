package io.pinect.azeron.client.service.publisher;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Publisher {
    Class forClass();
    boolean raw() default false;
    EventMessagePublisher.PublishStrategy publishStrategy() default EventMessagePublisher.PublishStrategy.AZERON_NO_FALLBACK;
    String eventName();
}
