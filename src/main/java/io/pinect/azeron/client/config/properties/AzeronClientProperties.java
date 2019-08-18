package io.pinect.azeron.client.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azeron.client")
@Getter
@Setter
public class AzeronClientProperties {
    private boolean unSubscribeWhenShuttingDown = false;
    private String azeronServerHost = "localhost";
    private int pingIntervalSeconds = 10;
    private int unseenQueryIntervalSeconds = 10;
    private int unProcessedRecheckIntervalSeconds = 60;
}
