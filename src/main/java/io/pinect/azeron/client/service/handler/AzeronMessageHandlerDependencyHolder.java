package io.pinect.azeron.client.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import io.pinect.azeron.client.domain.entity.MessageEntity;
import io.pinect.azeron.client.domain.repository.MessageRepository;
import io.pinect.azeron.client.service.lock.ProcessingLock;
import io.pinect.azeron.client.service.publisher.SeenPublisher;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
@Getter
public class AzeronMessageHandlerDependencyHolder {
    private final ObjectMapper objectMapper;
    private final MessageRepository messageRepository;
    private final SeenPublisher seenPublisher;
    private final Executor seenExecutor;
    private final ProcessingLock processingLock;
    private final Converter<MessageDto, MessageEntity> messageDtoToEntityConverter;

    @Autowired
    public AzeronMessageHandlerDependencyHolder(ObjectMapper objectMapper, MessageRepository messageRepository, SeenPublisher seenPublisher, Executor seenExecutor, ProcessingLock processingLock, Converter<MessageDto, MessageEntity> messageDtoToEntityConverter) {
        this.objectMapper = objectMapper;
        this.messageRepository = messageRepository;
        this.seenPublisher = seenPublisher;
        this.seenExecutor = seenExecutor;
        this.processingLock = processingLock;
        this.messageDtoToEntityConverter = messageDtoToEntityConverter;
    }
}