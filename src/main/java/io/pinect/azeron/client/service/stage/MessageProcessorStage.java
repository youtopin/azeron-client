package io.pinect.azeron.client.service.stage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.ProcessErrorStrategy;
import io.pinect.azeron.client.domain.entity.MessageEntity;
import io.pinect.azeron.client.domain.model.AzeronHandlerPiplineResult;
import io.pinect.azeron.client.domain.repository.MessageRepository;
import io.pinect.azeron.client.service.HandlerProvider;
import io.pinect.azeron.client.util.Stage;

public class MessageProcessorStage implements Stage<MessageEntity, AzeronHandlerPiplineResult> {
    private final HandlerProvider.AzeronMessageProcessor azeronMessageProcessor;
    private final HandlerProvider.AzeronErrorHandler azeronErrorHandler;
    private final MessageRepository messageRepository;
    private final ProcessErrorStrategy processErrorStrategy;
    private final HandlerPolicy handlerPolicy;
    private final ObjectMapper objectMapper;
    private final Class eClass;

    public MessageProcessorStage(HandlerProvider.AzeronMessageProcessor azeronMessageProcessor, HandlerProvider.AzeronErrorHandler azeronErrorHandler, MessageRepository messageRepository, ProcessErrorStrategy processErrorStrategy, HandlerPolicy handlerPolicy, ObjectMapper objectMapper, Class eClass) {
        this.azeronMessageProcessor = azeronMessageProcessor;
        this.azeronErrorHandler = azeronErrorHandler;
        this.messageRepository = messageRepository;
        this.processErrorStrategy = processErrorStrategy;
        this.handlerPolicy = handlerPolicy;
        this.objectMapper = objectMapper;
        this.eClass = eClass;
    }

    @Override
    public boolean process(MessageEntity messageEntity, AzeronHandlerPiplineResult o) {
        boolean processed = false;
        try{
            Object value = objectMapper.readValue(messageEntity.getMessage(), eClass);
            azeronMessageProcessor.process(value);
            processed = true;
        }catch (Exception e){
            azeronErrorHandler.onError(e);
        }

        if(handlerPolicy.equals(HandlerPolicy.PROC))
            return true;

        if(!processed && processErrorStrategy.equals(ProcessErrorStrategy.CONTINUE))
            messageRepository.processed(messageEntity);
        else if(!processed)
            messageRepository.processed(messageEntity);

        return true;
    }
}
