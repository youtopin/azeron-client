package io.pinect.azeron.client.util;

import io.netty.channel.nio.NioEventLoopGroup;
import io.pinect.azeron.client.domain.model.NatsConfigModel;
import nats.client.NatsConnector;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NatsConnectorProvider {
    public static NatsConnector getNatsConnector(NatsConfigModel natsConfig){
        NatsConnector natsConnector = new NatsConnector();
        natsConfig.getHosts().forEach(natsConnector::addHost);
        natsConnector.automaticReconnect(true);
        natsConnector.idleTimeout(natsConfig.getIdleTimeOut());
        natsConnector.pedantic(natsConfig.isPedanic());
        natsConnector.reconnectWaitTime(2 , TimeUnit.SECONDS);
        natsConnector.eventLoopGroup(new NioEventLoopGroup(500));
        natsConnector.calllbackExecutor(new ScheduledThreadPoolExecutor(200));
        natsConnector.pingInterval(5000);
        return natsConnector;
    }
}
