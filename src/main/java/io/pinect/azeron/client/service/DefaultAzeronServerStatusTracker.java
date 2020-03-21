package io.pinect.azeron.client.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class DefaultAzeronServerStatusTracker implements AzeronServerStatusTracker {
    private volatile Status status = Status.UP;
    private final EventListenerRegistry eventListenerRegistry;

    @Autowired
    public DefaultAzeronServerStatusTracker(EventListenerRegistry eventListenerRegistry) {
        this.eventListenerRegistry = eventListenerRegistry;
    }

    @Override
    public synchronized void setStatus(Status s) {
        boolean hasChanged = !status.equals(s);
        log.trace("Azeron status update: "+ s + " , changed: "+ hasChanged);
        this.status = s;
        if(hasChanged && isUp()){
            eventListenerRegistry.retryableReRegisterAll();
        }
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
