package io.pinect.azeron.client.service;

import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.service.api.NatsConfigProvider;
import io.pinect.azeron.client.service.stateListener.NatsConnectionStateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;

@Service
public class NatsReconnectForceService {
    private final NatsConnectionStateListener natsConnectionStateListener;
    private final NatsConnectionUpdater natsConnectionUpdater;
    private final Semaphore semaphore;

    @Autowired
    public NatsReconnectForceService(@Lazy NatsConnectionStateListener natsConnectionStateListener, @Lazy NatsConfigProvider natsConfigProvider, AtomicNatsHolder atomicNatsHolder, ApplicationContext applicationContext) {
        this.natsConnectionStateListener = natsConnectionStateListener;
        natsConnectionUpdater = new NatsConnectionUpdater(natsConfigProvider, atomicNatsHolder, applicationContext);
        semaphore = new Semaphore(1);
    }

    public void forceNatsReconnect(){
        boolean b = semaphore.tryAcquire();
        try {
            if(b)
                natsConnectionUpdater.update(natsConnectionStateListener);
        }finally {
            if(b)
                semaphore.release();
        }
    }


}
