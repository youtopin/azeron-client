package io.pinect.azeron.client.domain.dto.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.pinect.azeron.client.domain.dto.ResponseStatus;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnseenResponseDto extends BasicAzeronReponseDto {
    private boolean hasMore;
    private int count;
    private List<MessageDto> messages;

    public UnseenResponseDto() {
        super(ResponseStatus.OK);
    }
}
