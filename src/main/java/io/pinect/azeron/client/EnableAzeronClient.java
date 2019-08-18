package io.pinect.azeron.client;

import io.pinect.azeron.client.config.AzeronClientConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(AzeronClientConfiguration.class)
public @interface EnableAzeronClient {
}
