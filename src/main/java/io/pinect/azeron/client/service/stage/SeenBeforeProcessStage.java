package io.pinect.azeron.client.service.stage;

import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import io.pinect.azeron.client.domain.model.AzeronHandlerPiplineResult;
import io.pinect.azeron.client.service.handler.EventListener;
import io.pinect.azeron.client.service.publisher.SeenPublisher;
import io.pinect.azeron.client.util.Stage;

import java.util.concurrent.Executor;

public class SeenBeforeProcessStage implements Stage<MessageDto, AzeronHandlerPiplineResult> {
    private final SeenPublisher seenPublisher;
    private final HandlerPolicy handlerPolicy;
    private final EventListener.AzeronErrorHandler azeronErrorHandler;
    private final Executor seenExecutor;

    public SeenBeforeProcessStage(SeenPublisher seenPublisher, HandlerPolicy handlerPolicy, EventListener.AzeronErrorHandler azeronErrorHandler, Executor seenExecutor) {
        this.seenPublisher = seenPublisher;
        this.handlerPolicy = handlerPolicy;
        this.azeronErrorHandler = azeronErrorHandler;
        this.seenExecutor = seenExecutor;
    }

    @Override
    public boolean process(MessageDto messageDto, AzeronHandlerPiplineResult o) {
        if(handlerPolicy.equals(HandlerPolicy.NO_AZERON))
            return false;

        if(handlerPolicy.equals(HandlerPolicy.SEEN_FIRST)){
            seenExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        seenPublisher.publishSeen(messageDto.getMessageId());
                    } catch (Exception e) {
                        azeronErrorHandler.onError(e, messageDto);
                    }
                }
            });
        }

        return true;
    }
}
