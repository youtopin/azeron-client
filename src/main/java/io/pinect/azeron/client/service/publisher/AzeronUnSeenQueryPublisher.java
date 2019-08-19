package io.pinect.azeron.client.service.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.config.ChannelName;
import io.pinect.azeron.client.config.properties.AzeronClientProperties;
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
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Log4j2
public class AzeronUnSeenQueryPublisher extends EventMessagePublisher implements QueryPublisher {
    private final String serviceName;
    private final AzeronClientProperties azeronClientProperties;

    @Autowired
    public AzeronUnSeenQueryPublisher(AtomicNatsHolder atomicNatsHolder, ObjectMapper objectMapper, AzeronServerStatusTracker azeronServerStatusTracker, FallbackRepository fallbackRepository, RetryTemplate eventPublishRetryTemplate, @Value("${spring.application.name}") String serviceName, AzeronClientProperties azeronClientProperties) {
        super(atomicNatsHolder, objectMapper, azeronServerStatusTracker, fallbackRepository, eventPublishRetryTemplate, serviceName);
        this.serviceName = serviceName;
        this.azeronClientProperties = azeronClientProperties;
    }

    @Override
    public UnseenResponseDto publishQuery() throws Exception {
        UnseenQueryDto unseenQueryDto = UnseenQueryDto.builder().dateBefore(new Date().getTime() - ((azeronClientProperties.getUnseenQueryIntervalSeconds() + 1) * 1000)).serviceName(serviceName).build();
        AtomicReference<UnseenResponseDto> unseenResponseDto = new AtomicReference<>(UnseenResponseDto.builder().count(0).hasMore(false).build());
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
