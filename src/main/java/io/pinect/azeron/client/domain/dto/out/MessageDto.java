package io.pinect.azeron.client.domain.dto.out;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageDto implements Serializable {
    private String messageId;
    private String channelName;
    private long timeStamp;
    private JsonNode object;
    private String serviceName;
    private ProcessStatus status = ProcessStatus.FAILED;

    public enum ProcessStatus {
        PROCESSED, FAILED
    }
}