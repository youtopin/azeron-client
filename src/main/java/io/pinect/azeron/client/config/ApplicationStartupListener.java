package io.pinect.azeron.client.config;

import io.pinect.azeron.client.config.properties.AzeronClientProperties;
import io.pinect.azeron.client.service.AzeronServerStatusTracker;
import io.pinect.azeron.client.service.api.Pinger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {
    private final AzeronClientProperties azeronClientProperties;
    private final AzeronServerStatusTracker azeronServerStatusTracker;
    private final TaskScheduler azeronTaskScheduler;
    private ScheduledFuture<?> schedule;
    private final Pinger pinger;

    @Autowired
    public ApplicationStartupListener(AzeronClientProperties azeronClientProperties, AzeronServerStatusTracker azeronServerStatusTracker, TaskScheduler azeronTaskScheduler, Pinger pinger) {
        this.azeronClientProperties = azeronClientProperties;
        this.azeronServerStatusTracker = azeronServerStatusTracker;
        this.azeronTaskScheduler = azeronTaskScheduler;
        this.pinger = pinger;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        startPingTaskSchedule();
    }

    private void startPingTaskSchedule(){
        PeriodicTrigger periodicTrigger = new PeriodicTrigger(azeronClientProperties.getPingIntervalSeconds(), TimeUnit.SECONDS);
        periodicTrigger.setInitialDelay(azeronClientProperties.getPingIntervalSeconds() * 1000);
        this.schedule = azeronTaskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                AzeronServerStatusTracker.Status status = pinger.ping();
                azeronServerStatusTracker.setStatus(status);
            }
        }, periodicTrigger);
    }

    @PreDestroy
    public void destroy(){
        schedule.cancel(true);
    }
}