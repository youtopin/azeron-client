package io.pinect.azeron.client.domain.repository;

import io.pinect.azeron.client.domain.entity.MessageEntity;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class NullMessageRepository implements MessageRepository<MessageEntity> {
    public NullMessageRepository() {
        log.warn("Constructed `NullMessageRepository` as a MessageRepository. This might be used as default repository bean. Are you sure?");
    }

    @Override
    public MessageEntity save(MessageEntity messageEntity) {
        return null;
    }

    @Override
    public boolean exists(String messageId) {
        return false;
    }

    @Override
    public MessageEntity seen(MessageEntity messageEntity) {
        return null;
    }

    @Override
    public MessageEntity processed(MessageEntity messageEntity) {
        return null;
    }

    @Override
    public void delete(MessageEntity messageEntity) {

    }

    @Override
    public List<String> getUnseenMessageIds(long offset, int limit) {
        return null;
    }

    @Override
    public List<MessageEntity> getUnseenMessages(long offset, int limit) {
        return null;
    }

    @Override
    public List<String> getUnProcessedMessageIds(long offset, int limit) {
        return null;
    }

    @Override
    public List<MessageEntity> getUnProcessedMessages(long offset, int limit) {
        return null;
    }

    @Override
    public int countUnProcessed() {
        return 0;
    }

    @Override
    public MessageEntity findById(String id) {
        return null;
    }

    @Override
    public List<MessageEntity> findByIdIn(List<String> ids) {
        return null;
    }
}
