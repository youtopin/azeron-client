package io.pinect.azeron.client.converter;

import io.pinect.azeron.client.domain.dto.MessageDto;
import io.pinect.azeron.client.domain.entity.MessageEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import java.util.Date;

@Configuration
public class DtoConverter {

    @Bean("messageDtoToEntityConverter")
    public Converter<MessageDto, MessageEntity> toMessageEntity(){
        return new Converter<MessageDto, MessageEntity>() {
            @Override
            public MessageEntity convert(MessageDto messageDto) {
                return MessageEntity.builder()
                        .channelName(messageDto.getChannelName())
                        .date(new Date(messageDto.getTimeStamp()))
                        .message(messageDto.getObject().toString())
                        .messageId(messageDto.getMessageId())
                        .serviceName(messageDto.getServiceName())
                        .build();
            }
        };
    }
}
