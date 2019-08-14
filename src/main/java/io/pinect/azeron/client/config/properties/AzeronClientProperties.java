package io.pinect.azeron.client.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azeron.server")
public class AzeronClientProperties {
    private boolean unSubscribeWhenShuttingDown = false;

    public boolean isUnSubscribeWhenShuttingDown() {
        return unSubscribeWhenShuttingDown;
    }

    public void setUnSubscribeWhenShuttingDown(boolean unSubscribeWhenShuttingDown) {
        this.unSubscribeWhenShuttingDown = unSubscribeWhenShuttingDown;
    }
}
