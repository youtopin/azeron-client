package io.pinect.azeron.client.domain.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageEntity implements Serializable {
    private String messageId;
    private String message;
    private Date date;
    private String channelName;
    private String serviceName;
    private boolean isSeen;
    private boolean isProcessed;
}
