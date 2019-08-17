package io.pinect.azeron.client.service.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.config.ChannelName;
import io.pinect.azeron.client.domain.dto.in.UnseenResponseDto;
import io.pinect.azeron.client.domain.dto.out.UnseenQueryDto;
import io.pinect.azeron.client.domain.repository.FallbackRepository;
import io.pinect.azeron.client.service.AzeronServerStatusTracker;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Log4j2
public class AzeronUnSeenQueryPublisher extends EventMessagePublisher implements QueryPublisher {
    private final String serviceName;

    @Autowired
    public AzeronUnSeenQueryPublisher(AtomicNatsHolder atomicNatsHolder, ObjectMapper objectMapper, AzeronServerStatusTracker azeronServerStatusTracker, FallbackRepository fallbackRepository, RetryTemplate eventPublishRetryTemplate, @Value("${application.name}") String serviceName) {
        super(atomicNatsHolder, objectMapper, azeronServerStatusTracker, fallbackRepository, eventPublishRetryTemplate, serviceName);
        this.serviceName = serviceName;
    }

    @Override
    public UnseenResponseDto publishQuery() throws Exception {
        UnseenQueryDto unseenQueryDto = UnseenQueryDto.builder().serviceName(serviceName).build();
        AtomicReference<UnseenResponseDto> unseenResponseDto = new AtomicReference<>(null);
        sendMessage(ChannelName.AZERON_QUERY_CHANNEL_NAME, getObjectMapper().writeValueAsString(unseenQueryDto), PublishStrategy.BLOCKED, message -> {
            String messageBody = message.getBody();
            try {
                unseenResponseDto.set(getObjectMapper().readValue(messageBody, UnseenResponseDto.class));
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        });
        return unseenResponseDto.get();
    }
}
