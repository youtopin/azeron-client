package io.pinect.azeron.client.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString
public class NatsConfigModel {
    @Builder.Default
    private String host = "localhost";
    @Builder.Default
    private String hostIp = "127.0.0.1";
    @Builder.Default
    private String protocol="nats";
    @Builder.Default
    private String port = "4222";
    private boolean useEpoll;
    private int idleTimeOut;
    private boolean pedanic;
    private int reconnectWaitSeconds;
}
