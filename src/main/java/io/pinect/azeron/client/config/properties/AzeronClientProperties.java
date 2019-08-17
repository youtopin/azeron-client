package io.pinect.azeron.client.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azeron.server")
@Getter
@Setter
public class AzeronClientProperties {
    private boolean unSubscribeWhenShuttingDown = false;
    private String azeronServerHost = "localhost";
}
