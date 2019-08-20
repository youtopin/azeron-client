package io.pinect.azeron.client.service;

import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.domain.model.NatsConfigModel;
import io.pinect.azeron.client.service.api.NatsConfigProvider;
import io.pinect.azeron.client.service.stateListener.NatsConnectionStateListener;
import io.pinect.azeron.client.util.NatsConnectorProvider;
import lombok.extern.log4j.Log4j2;
import nats.client.Nats;
import nats.client.NatsConnector;
import nats.client.spring.ApplicationEventPublishingConnectionStateListener;
import org.springframework.context.ApplicationContext;

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
        try {
            Nats nats = atomicNatsHolder.getNatsAtomicReference().get();
            if(nats != null){
                nats.close();
            }
        }catch (Exception e){
            log.error(e);
        }

        NatsConfigModel natsConfig = natsConfigProvider.getNatsConfig();

        NatsConnector natsConnector = NatsConnectorProvider.getNatsConnector(natsConfig);
        natsConnector.addConnectionStateListener(new ApplicationEventPublishingConnectionStateListener(this.applicationContext));
        natsConnector.addConnectionStateListener(natsConnectionStateListener);
        Nats nats = natsConnector.connect();

        log.info("Successfully updated nats info");
        atomicNatsHolder.getNatsAtomicReference().set(nats);
    }
}
