package io.pinect.azeron.client.service.lock;

import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class SingleNodeHandlingLock implements HandlingLock {
    private final Map<String, Lock> lockMap = new ConcurrentHashMap<>();

    public SingleNodeHandlingLock() {
       log.info("Constructed instance of SingleNodeHandlingLock for HandlingLock bean");
    }

    @Override
    public Lock getLock(String id) {
        Lock lock = lockMap.computeIfAbsent(id, k -> new ReentrantLock());
        if(lock == null)
            lockMap.get(id);
        return lock;
    }

    @Override
    public void removeLock(String id) {
        lockMap.remove(id);
    }
}
