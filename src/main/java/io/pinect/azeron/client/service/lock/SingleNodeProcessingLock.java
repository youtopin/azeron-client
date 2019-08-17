package io.pinect.azeron.client.service.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SingleNodeProcessingLock implements ProcessingLock {
    private final Map<String, Lock> lockMap = new HashMap<>();

    @Override
    public Lock getLock(String id) {
        return lockMap.putIfAbsent(id, new ReentrantLock());
    }

    @Override
    public void removeLock(String id) {
        lockMap.remove(id);
    }
}
