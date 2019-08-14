package io.pinect.azeron.client.domain.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class MessageDto implements Serializable {
    private String messageId;
    private String channelName;
    private long timeStamp;
    private JsonNode object;
    private String serviceName;
}