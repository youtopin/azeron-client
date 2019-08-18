package io.pinect.azeron.client.service.api;

import io.pinect.azeron.client.domain.dto.in.UnseenResponseDto;
import io.pinect.azeron.client.service.EventListenerRegistry;
import io.pinect.azeron.client.service.handler.EventListener;
import io.pinect.azeron.client.service.publisher.AzeronUnSeenQueryPublisher;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Log4j2
public class UnseenRetrieveService {
    private final AzeronUnSeenQueryPublisher azeronUnSeenQueryPublisher;
    private final EventListenerRegistry eventListenerRegistry;
    private final Lock lock;

    @Autowired
    public UnseenRetrieveService(AzeronUnSeenQueryPublisher azeronUnSeenQueryPublisher, EventListenerRegistry eventListenerRegistry) {
        this.azeronUnSeenQueryPublisher = azeronUnSeenQueryPublisher;
        this.eventListenerRegistry = eventListenerRegistry;
        this.lock = new ReentrantLock();
    }

    public synchronized void execute(){
        boolean locked = lock.tryLock();
        try {
            if (locked) {
                UnseenResponseDto unseenResponseDto = azeronUnSeenQueryPublisher.publishQuery();
                unseenResponseDto.getMessages().forEach(messageDto -> {
                    EventListener eventListener = eventListenerRegistry.getEventListenerOfChannel(messageDto.getChannelName());
                    eventListener.handle(messageDto.getObject().toString());
                });
            }
        } catch (Exception e) {
            log.error(e);
        }finally {
            if(locked)
                lock.unlock();
        }
    }

}
