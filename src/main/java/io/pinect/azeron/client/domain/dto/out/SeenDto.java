package io.pinect.azeron.client.domain.dto.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SeenDto {
    private String messageId;
    private List<String> messageIds;
    @NotNull
    private String serviceName;
    private String reqId;

    @JsonIgnore
    @AssertTrue
    public boolean isValidMessage(){
        return messageId != null || messageIds != null;
    }
}
