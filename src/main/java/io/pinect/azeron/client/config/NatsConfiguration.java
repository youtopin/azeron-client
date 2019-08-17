package io.pinect.azeron.client.config;

import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.domain.model.NatsConfigModel;
import io.pinect.azeron.client.service.NatsConfigProvider;
import io.pinect.azeron.client.service.stateListener.NatsConnectionStateListener;
import nats.client.Nats;
import nats.client.NatsConnector;
import nats.client.spring.ApplicationEventPublishingConnectionStateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.TimeUnit;

@Configuration
public class NatsConfiguration {
    private final ApplicationContext applicationContext;
    private final NatsConfigProvider natsConfigProvider;
    private final NatsConnectionStateListener natsConnectionStateListener;

    @Autowired
    public NatsConfiguration(@Lazy NatsConfigProvider natsConfigProvider, ApplicationContext applicationContext, NatsConnectionStateListener natsConnectionStateListener) {
        this.natsConfigProvider = natsConfigProvider;
        this.applicationContext = applicationContext;
        this.natsConnectionStateListener = natsConnectionStateListener;
    }

    @Bean
    @DependsOn({"natsConfigProvider"})
    public AtomicNatsHolder atomicNatsHolder(){
        NatsConfigModel natsConfig = natsConfigProvider.getNatsConfig();
        NatsConnector natsConnector = new NatsConnector();
        natsConnector.addConnectionStateListener(new ApplicationEventPublishingConnectionStateListener(this.applicationContext));
        natsConnector.addConnectionStateListener(natsConnectionStateListener);
        natsConnector.addHost(natsConfig.getHost());
        natsConnector.automaticReconnect(true);
        natsConnector.idleTimeout(natsConfig.getIdleTimeOut());
        natsConnector.pedantic(natsConfig.isPedanic());
        natsConnector.reconnectWaitTime(5 , TimeUnit.SECONDS);
        Nats nats = natsConnector.connect();
        return new AtomicNatsHolder(nats);
    }

}
