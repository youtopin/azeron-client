package io.pinect.azeron.client.service;

import io.pinect.azeron.client.domain.entity.MessageEntity;
import io.pinect.azeron.client.domain.repository.MessageRepository;
import io.pinect.azeron.client.service.handler.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ProcessReCheckService {
    private final EventListenerRegistry eventListenerRegistry;
    private final MessageRepository messageRepository;
    private final Lock lock;

    @Autowired
    public ProcessReCheckService(EventListenerRegistry eventListenerRegistry, MessageRepository messageRepository) {
        this.eventListenerRegistry = eventListenerRegistry;
        this.messageRepository = messageRepository;
        this.lock = new ReentrantLock();
    }

    public void execute(){
        boolean b = lock.tryLock();
        try {
            if(b){
                int i = messageRepository.countUnProcessed();
                int offset = 0;
                int limit = 10;
                while (offset < i){
                    List<MessageEntity> messageEntityList = messageRepository.getUnProcessedMessages(offset, limit);
                    messageEntityList.forEach(messageEntity -> {
                        if (new Date().getTime() - messageEntity.getDate().getTime() > 20000){
                            EventListener eventListener = eventListenerRegistry.getEventListenerOfChannel(messageEntity.getChannelName());
                            eventListener.handle(messageEntity.getMessage());
                        }
                    });
                    offset += limit;
                }
            }
        }finally {
            if(b)
                lock.unlock();
        }
    }

}
