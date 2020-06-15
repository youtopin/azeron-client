package io.pinect.azeron.client.util;

import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.pinect.azeron.client.domain.model.NatsConfigModel;
import nats.client.NatsConnector;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NatsConnectorProvider {
    private static volatile NioEventLoopGroup nioEventLoopGroup;

    public static synchronized NioEventLoopGroup getNioEventLoopGroupInstance(){
        if(nioEventLoopGroup == null){
            nioEventLoopGroup = new NioEventLoopGroup(20);
        }
        return nioEventLoopGroup;
    }

    public static NatsConnector getNatsConnector(NatsConfigModel natsConfig){
        NatsConnector natsConnector = new NatsConnector();
        natsConfig.getHosts().forEach(natsConnector::addHost);
        natsConnector.automaticReconnect(true);
        natsConnector.idleTimeout(natsConfig.getIdleTimeOut());
        natsConnector.pedantic(natsConfig.isPedantic());
        natsConnector.reconnectWaitTime(2 , TimeUnit.SECONDS);
        if (natsConfig.isUseEpoll()) {
            natsConnector.eventLoopGroup(new EpollEventLoopGroup(500));
        }else {
            natsConnector.eventLoopGroup(getNioEventLoopGroupInstance());
        }
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(40);
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
