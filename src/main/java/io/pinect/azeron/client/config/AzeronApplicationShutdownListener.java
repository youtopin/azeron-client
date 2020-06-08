package io.pinect.azeron.client.config;

import io.netty.util.concurrent.Future;
import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.model.ClientConfig;
import io.pinect.azeron.client.service.listener.*;
import io.pinect.azeron.client.util.NatsConnectorProvider;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Map;

@Component
public class AzeronApplicationShutdownListener {
    private final AtomicNatsHolder atomicNatsHolder;

    public AzeronApplicationShutdownListener(AtomicNatsHolder atomicNatsHolder) {
        this.atomicNatsHolder = atomicNatsHolder;
    }

    @PreDestroy
    public void destroy(){
        try {
            atomicNatsHolder.getNatsAtomicReference().get().close();
        }catch (Exception ignored){}

        Future<?> future = NatsConnectorProvider.getNioEventLoopGroupInstance().shutdownGracefully();
        try {
            future.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
