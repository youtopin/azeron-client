package io.pinect.azeron.client.util;

import io.netty.channel.nio.NioEventLoopGroup;
import io.pinect.azeron.client.domain.model.NatsConfigModel;
import nats.client.NatsConnector;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NatsConnectorProvider {
    public static NatsConnector getNatsConnector(NatsConfigModel natsConfig){
        NatsConnector natsConnector = new NatsConnector();
        natsConnector.addHost(natsConfig.getProtocol() + "://" + natsConfig.getHost());
        natsConnector.automaticReconnect(true);
        natsConnector.idleTimeout(natsConfig.getIdleTimeOut());
        natsConnector.pedantic(natsConfig.isPedanic());
        natsConnector.reconnectWaitTime(2 , TimeUnit.SECONDS);
        natsConnector.eventLoopGroup(new NioEventLoopGroup());
        natsConnector.calllbackExecutor(new ScheduledThreadPoolExecutor(20));
        natsConnector.pingInterval(5000);
        return natsConnector;
    }
}
