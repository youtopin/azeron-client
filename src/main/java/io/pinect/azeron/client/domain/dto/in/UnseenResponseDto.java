package io.pinect.azeron.client.domain.dto.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.pinect.azeron.client.domain.dto.ResponseStatus;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class UnseenResponseDto extends BasicAzeronReponseDto {
    private boolean hasMore;
    private int count;
    private List<MessageDto> messages;

    public UnseenResponseDto() {
        super(ResponseStatus.OK);
    }
}
