package io.pinect.azeron.client.service;

import io.pinect.azeron.client.domain.model.NatsConfigModel;

public interface NatsConfigProvider {
    NatsConfigModel getNatsConfig();
}
