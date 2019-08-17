package io.pinect.azeron.client.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.ProcessErrorStrategy;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import io.pinect.azeron.client.domain.entity.MessageEntity;
import io.pinect.azeron.client.domain.model.AzeronHandlerPiplineResult;
import io.pinect.azeron.client.domain.repository.MessageRepository;
import io.pinect.azeron.client.service.lock.ProcessingLock;
import io.pinect.azeron.client.service.publisher.SeenPublisher;
import io.pinect.azeron.client.service.stage.MessageAddStage;
import io.pinect.azeron.client.service.stage.MessageProcessorStage;
import io.pinect.azeron.client.service.stage.SeenStage;
import io.pinect.azeron.client.util.Pipeline;
import nats.client.Message;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.util.concurrent.Executor;

public abstract class AbstractAzeronMessageHandler<E> implements EventListener<E> {
    private final ObjectMapper objectMapper;
    private final MessageRepository messageRepository;
    private final SeenPublisher seenPublisher;
    private final Executor seenExecutor;
    private final ProcessingLock processingLock;
    private final Converter<MessageDto, MessageEntity> messageDtoToEntityConverter;

    public AbstractAzeronMessageHandler(AzeronMessageHandlerDependencyHolder azeronMessageHandlerDependencyHolder) {
        this.objectMapper = azeronMessageHandlerDependencyHolder.getObjectMapper();
        this.messageRepository = azeronMessageHandlerDependencyHolder.getMessageRepository();
        this.seenPublisher = azeronMessageHandlerDependencyHolder.getSeenPublisher();
        this.seenExecutor = azeronMessageHandlerDependencyHolder.getSeenExecutor();
        this.processingLock = azeronMessageHandlerDependencyHolder.getProcessingLock();
        this.messageDtoToEntityConverter = azeronMessageHandlerDependencyHolder.getMessageDtoToEntityConverter();
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
            MessageEntity messageEntity = messageDtoToEntityConverter.convert(messageDto);
            Pipeline<MessageEntity, AzeronHandlerPiplineResult> pipeline = getPipeline(policy(), azeronMessageProcessor(), azeronErrorHandler(), eClass(), processErrorStrategy());
            AzeronHandlerPiplineResult azeronHandlerPiplineResult = new AzeronHandlerPiplineResult();
            pipeline.run(messageEntity, azeronHandlerPiplineResult);
        } catch (Exception e) {
            azeronErrorHandler().onError(e, messageDto);
        }
    }

    private Pipeline<MessageEntity, AzeronHandlerPiplineResult> getPipeline(HandlerPolicy policy, AzeronMessageProcessor azeronMessageProcessor, AzeronErrorHandler azeronErrorHandler, Class eClass, ProcessErrorStrategy processErrorStrategy){
        Pipeline<MessageEntity, AzeronHandlerPiplineResult> pipeline = new Pipeline<>();
        pipeline.addStage(new MessageAddStage(policy, messageRepository));
        pipeline.addStage(new SeenStage(policy, messageRepository, seenPublisher, seenExecutor));
        pipeline.addStage(new MessageProcessorStage(processingLock, azeronMessageProcessor, azeronErrorHandler, messageRepository, processErrorStrategy, policy, objectMapper, eClass));
        return pipeline;
    }

    private MessageDto getMessageDto(String body) throws IOException {
        return objectMapper.readValue(body, MessageDto.class);
    }

}
