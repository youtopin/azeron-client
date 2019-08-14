package io.pinect.azeron.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.ProcessErrorStrategy;
import io.pinect.azeron.client.domain.dto.MessageDto;
import io.pinect.azeron.client.domain.entity.MessageEntity;
import io.pinect.azeron.client.domain.model.AzeronHandlerPiplineResult;
import io.pinect.azeron.client.domain.repository.MessageRepository;
import io.pinect.azeron.client.service.publisher.SeenPublisher;
import io.pinect.azeron.client.service.stage.MessageAddStage;
import io.pinect.azeron.client.service.stage.MessageProcessorStage;
import io.pinect.azeron.client.service.stage.SeenStage;
import io.pinect.azeron.client.util.Pipeline;
import lombok.Setter;
import nats.client.Message;
import nats.client.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.Executor;

@Service
public class HandlerProvider {
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;
    private final SeenPublisher seenPublisher;
    private final Executor seenExecutor;
    private final Converter<MessageDto, MessageEntity> messageDtoToEntityConverter;

    @Autowired
    public HandlerProvider(MessageRepository messageRepository, ObjectMapper objectMapper, SeenPublisher seenPublisher, Executor seenExecutor, Converter<MessageDto, MessageEntity> messageDtoToEntityConverter) {
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
        this.seenPublisher = seenPublisher;
        this.seenExecutor = seenExecutor;
        this.messageDtoToEntityConverter = messageDtoToEntityConverter;
    }

    private Pipeline<MessageEntity, AzeronHandlerPiplineResult> getPipeline(HandlerPolicy policy, AzeronMessageProcessor azeronMessageProcessor, AzeronErrorHandler azeronErrorHandler, Class eClass, ProcessErrorStrategy processErrorStrategy){
        Pipeline<MessageEntity, AzeronHandlerPiplineResult> pipeline = new Pipeline<>();
        pipeline.addStage(new MessageAddStage(policy, messageRepository));
        pipeline.addStage(new SeenStage(policy, messageRepository, seenPublisher, seenExecutor));
        pipeline.addStage(new MessageProcessorStage(azeronMessageProcessor, azeronErrorHandler, messageRepository, processErrorStrategy, policy, objectMapper, eClass));
        return pipeline;
    }

    @Setter
    public class Handler<E> implements MessageHandler {
        private final HandlerPolicy policy;
        private final Class<E> eClass;
        private final AzeronMessageProcessor<E> azeronMessageProcessor;
        private final AzeronErrorHandler azeronErrorHandler;
        private ProcessErrorStrategy processErrorStrategy;

        public Handler(HandlerPolicy policy, Class<E> eClass, AzeronMessageProcessor<E> azeronMessageProcessor, AzeronErrorHandler azeronErrorHandler) {
            this.policy = policy;
            this.eClass = eClass;
            this.azeronMessageProcessor = azeronMessageProcessor;
            this.azeronErrorHandler = azeronErrorHandler;
            this.processErrorStrategy = ProcessErrorStrategy.FAILED;
        }


        @Override
        public void onMessage(Message message) {
            try {
                MessageDto messageDto = getMessageDto(message.getBody());
                MessageEntity messageEntity = messageDtoToEntityConverter.convert(messageDto);
                Pipeline<MessageEntity, AzeronHandlerPiplineResult> pipeline = getPipeline(policy, azeronMessageProcessor, azeronErrorHandler, eClass, processErrorStrategy);
                AzeronHandlerPiplineResult azeronHandlerPiplineResult = new AzeronHandlerPiplineResult();
                pipeline.run(messageEntity, azeronHandlerPiplineResult);
            } catch (Exception e) {
                azeronErrorHandler.onError(e);
            }
        }

        private MessageDto getMessageDto(String body) throws IOException {
            return objectMapper.readValue(body, MessageDto.class);
        }
    }

    public interface AzeronErrorHandler {
        void onError(Exception e);
    }

    public interface AzeronMessageProcessor<E> {
        void process(E e);
    }

}
