package io.pinect.azeron.client.domain.model;

import lombok.*;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString
public class NatsConfigModel {
    @Builder.Default
    private List<String> hosts = Collections.singletonList("nats://localhost:4222");
    private boolean useEpoll;
    private int idleTimeOut;
    private boolean pedanic;
    private int reconnectWaitSeconds;
}
