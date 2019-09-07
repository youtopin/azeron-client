package io.pinect.azeron.client.domain.model;

import lombok.*;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString
public class NatsConfigModel {
    @Builder.Default
    private List<String> hosts = Arrays.asList("nats://localhost:4222");
    private boolean useEpoll;
    private int idleTimeOut;
    private boolean pedantic;
    private int reconnectWaitSeconds;
    private int keepAliveSeconds = 20;
}
