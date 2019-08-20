package io.pinect.azeron.client.domain.dto.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.pinect.azeron.client.domain.dto.ResponseStatus;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class PongDto {
    @Builder.Default
    private ResponseStatus status = ResponseStatus.OK;
    @Builder.Default
    private boolean discovered = false;
    @Builder.Default
    private boolean askedForDiscovery = false;
}
