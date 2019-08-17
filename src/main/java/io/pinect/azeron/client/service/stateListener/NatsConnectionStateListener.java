package io.pinect.azeron.client.service.stateListener;

import nats.client.ConnectionStateListener;

public interface NatsConnectionStateListener extends ConnectionStateListener {
    State getCurrentState();
}
