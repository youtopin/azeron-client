package io.pinect.azeron.client.service;

import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.domain.model.NatsConfigModel;
import io.pinect.azeron.client.service.api.NatsConfigProvider;
import io.pinect.azeron.client.service.stateListener.NatsConnectionStateListener;
import lombok.extern.log4j.Log4j2;
import nats.client.Nats;
import nats.client.NatsConnector;
import nats.client.spring.ApplicationEventPublishingConnectionStateListener;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

@Log4j2
public class NatsConnectionUpdater {
    private final NatsConfigProvider natsConfigProvider;
    private final ApplicationContext applicationContext;
    private final AtomicNatsHolder atomicNatsHolder;

    public NatsConnectionUpdater(NatsConfigProvider natsConfigProvider, AtomicNatsHolder atomicNatsHolder, ApplicationContext applicationContext) {
        this.natsConfigProvider = natsConfigProvider;
        this.applicationContext = applicationContext;
        this.atomicNatsHolder = atomicNatsHolder;
    }

    public void update(NatsConnectionStateListener natsConnectionStateListener){
        log.info("Updating nats info");
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

        log.info("Successfully updated nats info");
        atomicNatsHolder.getNatsAtomicReference().set(nats);
    }
}
