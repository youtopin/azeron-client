package io.pinect.azeron.client.domain.dto.out;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto implements Serializable {
    private String messageId;
    private String channelName;
    private long timeStamp;
    private JsonNode object;
    private String serviceName;
}