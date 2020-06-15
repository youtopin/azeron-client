package io.pinect.azeron.client.config;

import io.pinect.azeron.client.service.publisher.PublisherProxy;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Proxy;

public class PublisherProxyBeanFactory {
    @Autowired
    PublisherProxy publisherProxy;

    @SuppressWarnings("unchecked")
    public <PS> PS createPublisherProxyBean(ClassLoader classLoader, Class<PS> clazz) {
        return (PS) Proxy.newProxyInstance(classLoader, new Class[] {clazz}, publisherProxy);
    }
}
