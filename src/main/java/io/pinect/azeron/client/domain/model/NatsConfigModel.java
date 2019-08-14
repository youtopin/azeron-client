package io.pinect.azeron.client.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@ToString
public class NatsConfigModel {
    private String host = "127.0.0.1";
    private String protocol="nats";
    private String port = "4222";
    private boolean useEpoll;
    private int idleTimeOut;
    private boolean pedanic;
    private int reconnectWaitSeconds;
}
