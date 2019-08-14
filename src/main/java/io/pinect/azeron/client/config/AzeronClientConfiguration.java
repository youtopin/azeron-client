package io.pinect.azeron.client.config;

import io.pinect.azeron.client.config.properties.AzeronClientProperties;
import io.pinect.azeron.client.domain.repository.MessageRepository;
import io.pinect.azeron.client.domain.repository.NullMessageRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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

}
