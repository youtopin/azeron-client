package io.pinect.azeron.client.domain.dto.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.pinect.azeron.client.domain.dto.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BasicAzeronResponseDto {
    private ResponseStatus status;
}
