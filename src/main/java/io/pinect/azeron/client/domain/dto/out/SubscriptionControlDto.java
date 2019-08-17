package io.pinect.azeron.client.domain.dto.out;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.pinect.azeron.client.domain.model.ClientConfig;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class SubscriptionControlDto {
    private String channelName;
    private ClientConfig config;
}
