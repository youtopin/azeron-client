package io.pinect.azeron.client.service.api;

import io.pinect.azeron.client.domain.dto.in.PongDto;

public interface Pinger {
    PongDto ping();
}
