package io.pinect.azeron.client.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Builder
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
