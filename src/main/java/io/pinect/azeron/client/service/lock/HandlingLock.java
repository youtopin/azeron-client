package io.pinect.azeron.client.service.lock;

import java.util.concurrent.locks.Lock;

public interface HandlingLock {
    Lock getLock(String id);
    void removeLock(String id);
}
