package io.pinect.azeron.client.service.stage;

import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.entity.MessageEntity;
import io.pinect.azeron.client.domain.model.AzeronHandlerPiplineResult;
import io.pinect.azeron.client.domain.repository.MessageRepository;
import io.pinect.azeron.client.service.publisher.SeenPublisher;
import io.pinect.azeron.client.util.Stage;

import java.util.concurrent.Executor;

public class SeenStage implements Stage<MessageEntity, AzeronHandlerPiplineResult> {
    private final HandlerPolicy handlerPolicy;
    private final MessageRepository messageRepository;
    private final SeenPublisher seenPublisher;
    private final Executor executor;

    public SeenStage(HandlerPolicy handlerPolicy, MessageRepository messageRepository, SeenPublisher seenPublisher, Executor executor) {
        this.handlerPolicy = handlerPolicy;
        this.messageRepository = messageRepository;
        this.seenPublisher = seenPublisher;
        this.executor = executor;
    }

    @Override
    public synchronized boolean process(MessageEntity messageEntity, AzeronHandlerPiplineResult o) {
        if(handlerPolicy.equals(HandlerPolicy.PROC) || handlerPolicy.equals(HandlerPolicy.LOSABLE))
            return true;

        if(handlerPolicy.equals(HandlerPolicy.ASYNC_SEEN)){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    publishSeenAndUpdateDb(messageEntity);
                }
            });
        }else{
            publishSeenAndUpdateDb(messageEntity);
        }
        return true;
    }

    private void publishSeenAndUpdateDb(MessageEntity messageEntity) {
        seenPublisher.publishSeen(messageEntity.getMessageId());
        messageRepository.seen(messageEntity);
    }
}
