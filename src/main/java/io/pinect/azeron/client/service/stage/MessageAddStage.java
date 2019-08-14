package io.pinect.azeron.client.service.stage;

import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.entity.MessageEntity;
import io.pinect.azeron.client.domain.model.AzeronHandlerPiplineResult;
import io.pinect.azeron.client.domain.repository.MessageRepository;
import io.pinect.azeron.client.util.Stage;

public class MessageAddStage implements Stage<MessageEntity, AzeronHandlerPiplineResult> {
    private final HandlerPolicy handlerPolicy;
    private final MessageRepository messageRepository;

    public MessageAddStage(HandlerPolicy handlerPolicy, MessageRepository messageRepository) {
        this.handlerPolicy = handlerPolicy;
        this.messageRepository = messageRepository;
    }

    @Override
    public boolean process(MessageEntity messageEntity, AzeronHandlerPiplineResult o) {
        if(handlerPolicy.equals(HandlerPolicy.LOSABLE))
            return true;
        if(handlerPolicy.equals(HandlerPolicy.PROC))
            messageEntity.setSeen(true);
        messageRepository.save(messageEntity);
        return true;
    }
}
