package io.pinect.azeron.client.service.listener;

import io.pinect.azeron.client.domain.HandlerPolicy;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
public @interface AzeronListener {
    HandlerPolicy policy() default HandlerPolicy.FULL;
    Class ofClass();
    int version() default 1;
    String eventName();
    boolean useQueueGroup() default true;
}
