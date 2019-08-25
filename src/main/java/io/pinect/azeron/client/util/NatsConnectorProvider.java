package io.pinect.azeron.client.util;

import io.netty.channel.nio.NioEventLoopGroup;
import io.pinect.azeron.client.domain.model.NatsConfigModel;
import nats.client.NatsConnector;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NatsConnectorProvider {
    public static NatsConnector getNatsConnector(NatsConfigModel natsConfig){
        NatsConnector natsConnector = new NatsConnector();
        natsConfig.getHosts().forEach(natsConnector::addHost);
        natsConnector.automaticReconnect(true);
        natsConnector.idleTimeout(natsConfig.getIdleTimeOut());
        natsConnector.pedantic(natsConfig.isPedantic());
        natsConnector.reconnectWaitTime(2 , TimeUnit.SECONDS);
        natsConnector.eventLoopGroup(new NioEventLoopGroup(500));
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(200);
        scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        scheduledThreadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        scheduledThreadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        scheduledThreadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduledThreadPoolExecutor.setKeepAliveTime(natsConfig.getKeepAliveSeconds(), TimeUnit.SECONDS);
        natsConnector.calllbackExecutor(scheduledThreadPoolExecutor);
        natsConnector.pingInterval(5000);
        return natsConnector;
    }
}
