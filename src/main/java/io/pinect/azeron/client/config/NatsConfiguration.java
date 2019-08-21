package io.pinect.azeron.client.config;

import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.domain.model.NatsConfigModel;
import io.pinect.azeron.client.service.api.NatsConfigProvider;
import io.pinect.azeron.client.service.stateListener.NatsConnectionStateListener;
import io.pinect.azeron.client.util.NatsConnectorProvider;
import lombok.extern.log4j.Log4j2;
import nats.client.Nats;
import nats.client.NatsConnector;
import nats.client.spring.ApplicationEventPublishingConnectionStateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

@Configuration
@Log4j2
public class NatsConfiguration {
    private final ApplicationContext applicationContext;
    private final NatsConfigProvider natsConfigProvider;
    private final NatsConnectionStateListener natsConnectionStateListener;

    @Autowired
    public NatsConfiguration(NatsConfigProvider natsConfigProvider, ApplicationContext applicationContext,@Lazy NatsConnectionStateListener natsConnectionStateListener) {
        this.natsConfigProvider = natsConfigProvider;
        this.applicationContext = applicationContext;
        this.natsConnectionStateListener = natsConnectionStateListener;
    }

    @Bean
    @DependsOn({"natsConfigProvider"})
    public AtomicNatsHolder atomicNatsHolder(){
        NatsConfigModel natsConfig = natsConfigProvider.getNatsConfig();
        log.trace("Found nats config model "+ natsConfig);
        NatsConnector natsConnector = NatsConnectorProvider.getNatsConnector(natsConfig);
        natsConnector.addConnectionStateListener(new ApplicationEventPublishingConnectionStateListener(this.applicationContext));
        natsConnector.addConnectionStateListener(natsConnectionStateListener);
        Nats nats = natsConnector.connect();
        return new AtomicNatsHolder(nats);
    }

}
