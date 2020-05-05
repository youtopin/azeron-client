package io.pinect.azeron.client;

import io.pinect.azeron.client.config.AzeronClientConfiguration;
import io.pinect.azeron.client.config.PublisherProxyBeansRegistrar;
import io.pinect.azeron.client.config.PublisherProxyConfig;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({
        AzeronClientConfiguration.class,
        PublisherProxyConfig.class,
        PublisherProxyBeansRegistrar.class
})
public @interface EnableAzeronClient {
    @AliasFor("basePackages")
    String[] value() default {};

    @AliasFor("value")
    String[] basePackages() default {};
}
