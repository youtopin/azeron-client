package io.pinect.azeron.client.service.api;

import io.pinect.azeron.client.config.properties.AzeronClientProperties;
import io.pinect.azeron.client.domain.dto.in.InfoResultDto;
import io.pinect.azeron.client.domain.model.NatsConfigModel;
import io.pinect.azeron.client.util.NatsConfigurationMerge;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Log4j2
public class HostBasedNatsConfigProvider implements NatsConfigProvider {
    private final RestTemplate restTemplate;
    private final AzeronClientProperties azeronClientProperties;
    private final RetryTemplate retryTemplate;


    public HostBasedNatsConfigProvider(RestTemplate restTemplate, AzeronClientProperties azeronClientProperties) {
        this.restTemplate = restTemplate;
        this.azeronClientProperties = azeronClientProperties;

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(5000); // 5 seconds

        this.retryTemplate = new RetryTemplate();
        this.retryTemplate.setRetryPolicy(new AlwaysRetryPolicy());
        this.retryTemplate.setBackOffPolicy(backOffPolicy);
    }

    @Override
    public NatsConfigModel getNatsConfig() {
        try {
            return this.retryTemplate.execute(new RetryCallback<NatsConfigModel, Throwable>() {
                @Override
                public NatsConfigModel doWithRetry(RetryContext retryContext) throws Throwable {
                    ResponseEntity<InfoResultDto> responseEntity = restTemplate.getForEntity("http://" + azeronClientProperties.getAzeronServerHost() + "/api/v1/info", InfoResultDto.class);
                    if(responseEntity.getStatusCode().equals(HttpStatus.OK) && responseEntity.hasBody()){
                        InfoResultDto infoResultDto = responseEntity.getBody();
                        if(infoResultDto.getResults() == null || infoResultDto.getResults().size() == 0)
                            throw new RuntimeException("Could not fetch info from server");

                        return NatsConfigurationMerge.getMergedNatsConfig(infoResultDto.getResults());
                    }
                    throw new Exception("Failed to get server info");
                }
            });
        } catch (Throwable throwable) {
            log.error(throwable);
            throw new RuntimeException(throwable);
        }
    }


}
