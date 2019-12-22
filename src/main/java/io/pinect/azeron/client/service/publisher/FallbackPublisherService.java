package io.pinect.azeron.client.service.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.domain.repository.FallbackRepository;
import io.pinect.azeron.client.service.AzeronServerStatusTracker;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Log4j2
public class FallbackPublisherService extends EventMessagePublisher{
    private final Lock lock;
    private final FallbackRepository fallbackRepository;
    private final AzeronServerStatusTracker azeronServerStatusTracker;

    @Autowired
    public FallbackPublisherService(AtomicNatsHolder atomicNatsHolder, ObjectMapper objectMapper, AzeronServerStatusTracker azeronServerStatusTracker, FallbackRepository fallbackRepository, RetryTemplate eventPublishRetryTemplate, @Value("${spring.application.name}") String serviceName) {
        super(atomicNatsHolder, objectMapper, azeronServerStatusTracker, fallbackRepository, eventPublishRetryTemplate, serviceName);
        this.azeronServerStatusTracker = azeronServerStatusTracker;
        this.fallbackRepository = fallbackRepository;
        lock = new ReentrantLock();
    }

    public void execute(){
        log.trace("Executing fallback publish");
        boolean locked = lock.tryLock();
        try {
            if (locked) {
                if(azeronServerStatusTracker.isDown())
                    return;

                int i = fallbackRepository.countAll();
                int offset = 0;
                int limit = 20;

                while (offset < i){
                    fallbackRepository.getMessages(offset, limit).forEach(fallbackEntity -> {
                        try {
                            log.trace("Re publishing message " + fallbackEntity.getMessage());
                            sendMessage(fallbackEntity.getEventName(), fallbackEntity.getMessage(), PublishStrategy.AZERON_NO_FALLBACK);
                            fallbackRepository.deleteMessage(fallbackEntity);
                        } catch (Exception e) {
                            log.error("Failed to send message from fallback repository.", e);
                        }
                    });
                    offset += limit;
                }
            }else{
                log.warn("Fallback publish service execution is called in short period. Previous call is not finished yet and is holding the lock.");
            }
        } catch (Exception e) {
            log.catching(e);
        }finally {
            if(locked)
                lock.unlock();
        }
    }



}
