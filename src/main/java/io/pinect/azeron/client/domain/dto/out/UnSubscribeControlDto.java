package io.pinect.azeron.client.domain.dto.out;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UnSubscribeControlDto {
    private String channelName;
    private String serviceName;
}
