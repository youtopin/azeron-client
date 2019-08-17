package io.pinect.azeron.client.config;

import io.pinect.azeron.client.config.properties.AzeronClientProperties;
import io.pinect.azeron.client.domain.repository.FallbackRepository;
import io.pinect.azeron.client.domain.repository.MessageRepository;
import io.pinect.azeron.client.domain.repository.NullMessageRepository;
import io.pinect.azeron.client.service.NatsConfigProvider;
import io.pinect.azeron.client.service.lock.ProcessingLock;
import io.pinect.azeron.client.service.lock.SingleNodeProcessingLock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAutoConfiguration
@ComponentScan("io.pinect.azeron.client")
@EnableConfigurationProperties({AzeronClientProperties.class})
public class AzeronClientConfiguration {

    @Bean
    @ConditionalOnMissingBean(MessageRepository.class)
    public MessageRepository messageRepository(){
        return new NullMessageRepository();
    }

    @Bean("seenExecutor")
    public Executor seenExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setQueueCapacity(100);
        threadPoolTaskExecutor.setMaxPoolSize(100);
        threadPoolTaskExecutor.setCorePoolSize(20);
        threadPoolTaskExecutor.setDaemon(true);
        threadPoolTaskExecutor.setThreadNamePrefix("seen_executor_");
        threadPoolTaskExecutor.setBeanName("seenExecutor");
        threadPoolTaskExecutor.setAwaitTerminationSeconds(10);
        return threadPoolTaskExecutor;
    }

    @Bean("eventPublishRetryTemplate")
    @Scope("singleton")
    public RetryTemplate eventPublishRetryTemplate(){
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(5000); // 5 seconds

        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(new AlwaysRetryPolicy());
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

    @Bean
    @ConditionalOnMissingBean(ProcessingLock.class)
    public ProcessingLock processingLock(){
        return new SingleNodeProcessingLock();
    }

    @Bean
    @ConditionalOnMissingBean(NatsConfigProvider.class)
    public NatsConfigProvider natsConfigProvider(){
        //todo
        return null;
    }

    @Bean
    @ConditionalOnMissingBean(FallbackRepository.class)
    public FallbackRepository fallbackRepository(){
        return new FallbackRepository.VoidFallbackRepository();
    }

}
