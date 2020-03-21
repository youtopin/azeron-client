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
    private boolean retrieveUnseen = true;
    private int unseenQueryIntervalSeconds = 20;
    private int fallbackPublishIntervalSeconds = 20;
    private int seenPublishSemaphoreSize = 20;
    private int natsRequestTimeoutSeconds = 10;
}
