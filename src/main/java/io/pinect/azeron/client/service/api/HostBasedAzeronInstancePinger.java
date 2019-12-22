package io.pinect.azeron.client.service.api;

import io.pinect.azeron.client.config.properties.AzeronClientProperties;
import io.pinect.azeron.client.domain.dto.ResponseStatus;
import io.pinect.azeron.client.domain.dto.in.PongDto;
import io.pinect.azeron.client.service.AzeronServerStatusTracker;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Log4j2
public class HostBasedAzeronInstancePinger implements Pinger {
    private final RestTemplate restTemplate;
    private final AzeronClientProperties azeronClientProperties;

    public HostBasedAzeronInstancePinger(AzeronClientProperties azeronClientProperties) {
        this.azeronClientProperties = azeronClientProperties;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public PongDto ping() {
        try {
            ResponseEntity<PongDto> responseEntity = restTemplate.getForEntity("http://" + azeronClientProperties.getAzeronServerHost() + "/api/v1/ping", PongDto.class);
            if(responseEntity.getStatusCode().equals(HttpStatus.OK) && responseEntity.hasBody())
                return responseEntity.getBody();
        }catch (Exception e){
            log.catching(e);
        }
        return PongDto.builder().status(ResponseStatus.FAILED).build();
    }
}
