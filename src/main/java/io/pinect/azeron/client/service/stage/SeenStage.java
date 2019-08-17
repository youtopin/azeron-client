package io.pinect.azeron.client.service.stage;

import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.entity.MessageEntity;
import io.pinect.azeron.client.domain.model.AzeronHandlerPiplineResult;
import io.pinect.azeron.client.domain.repository.MessageRepository;
import io.pinect.azeron.client.service.handler.EventListener;
import io.pinect.azeron.client.service.publisher.SeenPublisher;
import io.pinect.azeron.client.util.Stage;

import java.util.concurrent.Executor;

public class SeenStage implements Stage<MessageEntity, AzeronHandlerPiplineResult> {
    private final HandlerPolicy handlerPolicy;
    private final MessageRepository messageRepository;
    private final EventListener.AzeronErrorHandler azeronErrorHandler;
    private final SeenPublisher seenPublisher;
    private final Executor executor;

    public SeenStage(HandlerPolicy handlerPolicy, MessageRepository messageRepository, EventListener.AzeronErrorHandler azeronErrorHandler, SeenPublisher seenPublisher, Executor executor) {
        this.handlerPolicy = handlerPolicy;
        this.messageRepository = messageRepository;
        this.azeronErrorHandler = azeronErrorHandler;
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
            return publishSeenAndUpdateDb(messageEntity);
        }
        return true;
    }

    private boolean publishSeenAndUpdateDb(MessageEntity messageEntity) {
        try {
            seenPublisher.publishSeen(messageEntity.getMessageId());
        } catch (Exception e) {
            azeronErrorHandler.onError(e, null);
            return false;
        }
        messageRepository.seen(messageEntity);
        return true;
    }
}
