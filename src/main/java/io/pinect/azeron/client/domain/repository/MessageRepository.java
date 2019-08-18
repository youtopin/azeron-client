package io.pinect.azeron.client.domain.repository;

import io.pinect.azeron.client.domain.entity.MessageEntity;

import java.util.List;

public interface MessageRepository<E extends MessageEntity> {
    E save(E messageEntity);
    boolean exists(String messageId);
    MessageEntity seen(E messageEntity);
    MessageEntity processed(E messageEntity);
    void delete(MessageEntity messageEntity);
    List<String> getUnseenMessageIds(long offset, int limit);
    List<E> getUnseenMessages(long offset, int limit);
    List<String> getUnProcessedMessageIds(long offset, int limit);
    List<E> getUnProcessedMessages(long offset, int limit);
    int countUnProcessed();
    E findById(String id);
    List<E> findByIdIn(List<String> ids);
}
