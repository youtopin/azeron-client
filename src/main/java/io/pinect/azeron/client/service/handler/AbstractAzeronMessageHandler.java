package io.pinect.azeron.client.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import io.pinect.azeron.client.domain.model.AzeronHandlerPiplineResult;
import io.pinect.azeron.client.service.lock.HandlingLock;
import io.pinect.azeron.client.service.publisher.SeenPublisher;
import io.pinect.azeron.client.service.stage.MessageProcessorStage;
import io.pinect.azeron.client.service.stage.SeenAfterProcessStage;
import io.pinect.azeron.client.service.stage.SeenBeforeProcessStage;
import io.pinect.azeron.client.util.Pipeline;
import lombok.extern.log4j.Log4j2;
import nats.client.Message;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;

@Log4j2
public abstract class AbstractAzeronMessageHandler<E> implements EventListener<E> {
    private final ObjectMapper objectMapper;
    private final SeenPublisher seenPublisher;
    private final Executor seenExecutor;
    private final HandlingLock handlingLock;

    public AbstractAzeronMessageHandler(AzeronMessageHandlerDependencyHolder azeronMessageHandlerDependencyHolder) {
        this.objectMapper = azeronMessageHandlerDependencyHolder.getObjectMapper();
        this.seenPublisher = azeronMessageHandlerDependencyHolder.getSeenPublisher();
        this.seenExecutor = azeronMessageHandlerDependencyHolder.getSeenExecutor();
        this.handlingLock = azeronMessageHandlerDependencyHolder.getHandlingLock();
    }

    @Override
    public void onMessage(Message message) {
        handle(message.getBody());
    }

    @Override
    public void handle(String messageBody) {
        MessageDto messageDto = null;
        try {
            messageDto = getMessageDto(messageBody);
        } catch (IOException e) {
            log.error(e);
            azeronErrorHandler().onError(e, null);
        }
    }

    @Override
    public void handle(MessageDto messageDto){
        try {
            Lock lock = handlingLock.getLock(messageDto.getMessageId());
            boolean b = lock.tryLock();
            try {
                if(b){
                    Pipeline<MessageDto, AzeronHandlerPiplineResult> pipeline = getPipeline(policy(), azeronMessageProcessor(), azeronErrorHandler(), eClass());
                    AzeronHandlerPiplineResult azeronHandlerPiplineResult = new AzeronHandlerPiplineResult();
                    pipeline.run(messageDto, azeronHandlerPiplineResult);
                }
            }finally {
                if(b){
                    lock.unlock();
                    handlingLock.removeLock(messageDto.getMessageId());
                }
            }
        } catch (Exception e) {
            azeronErrorHandler().onError(e, messageDto);
        }
    }

    private Pipeline<MessageDto, AzeronHandlerPiplineResult> getPipeline(HandlerPolicy policy, AzeronMessageProcessor azeronMessageProcessor, AzeronErrorHandler azeronErrorHandler, Class eClass){
        Pipeline<MessageDto, AzeronHandlerPiplineResult> pipeline = new Pipeline<>();
        pipeline.addStage(new SeenBeforeProcessStage(seenPublisher, policy, azeronErrorHandler, seenExecutor));
        pipeline.addStage(new MessageProcessorStage(azeronMessageProcessor, azeronErrorHandler, objectMapper, eClass));
        pipeline.addStage(new SeenAfterProcessStage(seenPublisher, policy, azeronErrorHandler, seenExecutor));
        return pipeline;
    }

    private MessageDto getMessageDto(String body) throws IOException {
        return objectMapper.readValue(body, MessageDto.class);
    }

}
