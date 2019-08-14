package io.pinect.azeron.client.domain.repository;

import io.pinect.azeron.client.domain.entity.MessageEntity;

import java.util.List;

public interface MessageRepository {
    void save(MessageEntity messageEntity);
    boolean exists(String messageId);
    MessageEntity seen(MessageEntity messageEntity);
    MessageEntity processed(MessageEntity messageEntity);
    void delete(MessageEntity messageEntity);
    List<String> getUnseenMessageIds(long offset, int limit);
    List<MessageEntity> getUnseenMessages(long offset, int limit);
    List<String> getUnProcessedMessageIds(long offset, int limit);
    List<MessageEntity> getUnProcessedMessages(long offset, int limit);
    MessageEntity findById(String id);
    MessageEntity findByIdIn(List<String> ids);
}
