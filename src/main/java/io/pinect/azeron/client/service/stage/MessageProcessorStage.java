package io.pinect.azeron.client.service.stage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import io.pinect.azeron.client.domain.model.AzeronHandlerPiplineResult;
import io.pinect.azeron.client.service.handler.AbstractAzeronMessageHandler;
import io.pinect.azeron.client.util.Stage;

public class MessageProcessorStage implements Stage<MessageDto, AzeronHandlerPiplineResult> {
    private final AbstractAzeronMessageHandler.AzeronMessageProcessor azeronMessageProcessor;
    private final AbstractAzeronMessageHandler.AzeronErrorHandler azeronErrorHandler;
    private final ObjectMapper objectMapper;
    private final Class eClass;

    public MessageProcessorStage(AbstractAzeronMessageHandler.AzeronMessageProcessor azeronMessageProcessor, AbstractAzeronMessageHandler.AzeronErrorHandler azeronErrorHandler, ObjectMapper objectMapper, Class eClass) {
        this.azeronMessageProcessor = azeronMessageProcessor;
        this.azeronErrorHandler = azeronErrorHandler;
        this.objectMapper = objectMapper;
        this.eClass = eClass;
    }

    @Override
    public boolean process(MessageDto messageDto, AzeronHandlerPiplineResult o) {
        try{
            azeronMessageProcessor.process(objectMapper.readValue(messageDto.getObject().toString(), eClass));
            messageDto.setStatus(MessageDto.ProcessStatus.PROCESSED);
        } catch (Exception e) {
            azeronErrorHandler.onError(e, messageDto);
            return false;
        }

        return true;
    }
}
