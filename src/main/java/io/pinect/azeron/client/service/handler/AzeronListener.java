package io.pinect.azeron.client.service.handler;

import io.pinect.azeron.client.domain.HandlerPolicy;

public @interface AzeronListener {
    HandlerPolicy policy() default HandlerPolicy.FULL;
    Class ofClass();
    int version() default 1;
    String eventName();
    boolean useQueueGroup() default true;
}
