package io.pinect.azeron.client.service.publisher;

import io.pinect.azeron.client.domain.dto.in.UnseenResponseDto;

public interface QueryPublisher {
    UnseenResponseDto publishQuery() throws Exception;
}
