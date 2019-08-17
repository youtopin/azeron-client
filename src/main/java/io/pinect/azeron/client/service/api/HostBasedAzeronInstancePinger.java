package io.pinect.azeron.client.service.api;

import io.pinect.azeron.client.config.properties.AzeronClientProperties;
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
    public AzeronServerStatusTracker.Status ping() {
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://" + azeronClientProperties.getAzeronServerHost() + "/api/v1/ping", String.class);
            if(responseEntity.getStatusCode().equals(HttpStatus.OK) && responseEntity.hasBody() && responseEntity.getBody().toLowerCase().equals("pong"))
                return AzeronServerStatusTracker.Status.UP;
        }catch (Exception e){
            log.error(e);
        }
        return AzeronServerStatusTracker.Status.DOWN;
    }
}
