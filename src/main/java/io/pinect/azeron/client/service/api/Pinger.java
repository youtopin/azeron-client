package io.pinect.azeron.client.service.api;

import io.pinect.azeron.client.service.AzeronServerStatusTracker;

public interface Pinger {
    AzeronServerStatusTracker.Status ping();
}
