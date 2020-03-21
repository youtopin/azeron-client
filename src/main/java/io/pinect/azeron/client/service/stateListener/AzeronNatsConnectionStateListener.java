package io.pinect.azeron.client.service.stateListener;

import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.service.TaskScheduleInitializerService;
import io.pinect.azeron.client.service.EventListenerRegistry;
import io.pinect.azeron.client.service.NatsConnectionUpdater;
import io.pinect.azeron.client.service.api.NatsConfigProvider;
import lombok.extern.log4j.Log4j2;
import nats.client.ConnectionStateListener;
import nats.client.Nats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service("natsConnectionStateListener")
@Log4j2
public class AzeronNatsConnectionStateListener implements NatsConnectionStateListener{
    private ConnectionStateListener.State state;
    private final NatsConnectionUpdater natsConnectionUpdater;
    private final EventListenerRegistry eventListenerRegistry;
    private final TaskScheduleInitializerService taskScheduleInitializerService;

    @Autowired
    public AzeronNatsConnectionStateListener(NatsConfigProvider natsConfigProvider, AtomicNatsHolder atomicNatsHolder, ApplicationContext applicationContext, EventListenerRegistry eventListenerRegistry, TaskScheduleInitializerService taskScheduleInitializerService) {
        this.eventListenerRegistry = eventListenerRegistry;
        this.taskScheduleInitializerService = taskScheduleInitializerService;
        this.natsConnectionUpdater = new NatsConnectionUpdater(natsConfigProvider, atomicNatsHolder, applicationContext);
    }

    @Override
    public ConnectionStateListener.State getCurrentState() {
        return this.state;
    }

    @Override
    public void onConnectionStateChange(Nats nats, ConnectionStateListener.State state) {
        log.info("Nats state changed from "+ this.state + " to "+ state);
        switch (state){
            case CONNECTED:
                eventListenerRegistry.retryableReRegisterAll();
                taskScheduleInitializerService.destroy();
                taskScheduleInitializerService.initialize();
                break;
            case DISCONNECTED:
                taskScheduleInitializerService.destroy();
                natsConnectionUpdater.update(this);
                break;
        }
        this.state = state;
    }
}
