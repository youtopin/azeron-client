package io.pinect.azeron.client.service;

import io.pinect.azeron.client.domain.model.NatsConfigModel;
import io.pinect.azeron.client.domain.model.NatsConfigModelContainedEntity;

import java.util.List;

public interface NatsConfigChoserService {
    NatsConfigModel getBestNatsConfig(List<? extends NatsConfigModelContainedEntity> list);
}
