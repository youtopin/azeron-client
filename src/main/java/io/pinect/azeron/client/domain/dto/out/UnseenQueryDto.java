package io.pinect.azeron.client.domain.dto.out;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnseenQueryDto {
    private String serviceName;
}
