package io.pinect.azeron.client.config;

import io.pinect.azeron.client.config.properties.AzeronClientProperties;
import io.pinect.azeron.client.domain.dto.ResponseStatus;
import io.pinect.azeron.client.domain.dto.in.PongDto;
import io.pinect.azeron.client.service.AzeronServerStatusTracker;
import io.pinect.azeron.client.service.EventListenerRegistry;
import io.pinect.azeron.client.service.api.Pinger;
import io.pinect.azeron.client.service.api.UnseenRetrieveService;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {
    private final AzeronClientProperties azeronClientProperties;
    private final EventListenerRegistry eventListenerRegistry;
    private final AzeronServerStatusTracker azeronServerStatusTracker;
    private final UnseenRetrieveService unseenRetrieveService;
    private final TaskScheduler azeronTaskScheduler;
    private ScheduledFuture<?> pingSchedule;
    private ScheduledFuture<?> unseenSchedule;
    private final Pinger pinger;

    @Autowired
    public ApplicationStartupListener(AzeronClientProperties azeronClientProperties, EventListenerRegistry eventListenerRegistry, AzeronServerStatusTracker azeronServerStatusTracker, UnseenRetrieveService unseenRetrieveService, TaskScheduler azeronTaskScheduler, Pinger pinger) {
        this.azeronClientProperties = azeronClientProperties;
        this.eventListenerRegistry = eventListenerRegistry;
        this.azeronServerStatusTracker = azeronServerStatusTracker;
        this.unseenRetrieveService = unseenRetrieveService;
        this.azeronTaskScheduler = azeronTaskScheduler;
        this.pinger = pinger;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        startPingTaskSchedule();
        startUnseenRetrieveSchedule();
    }

    private void startPingTaskSchedule(){
        log.trace("Starting ping task schedule");
        PeriodicTrigger periodicTrigger = new PeriodicTrigger(azeronClientProperties.getPingIntervalSeconds(), TimeUnit.SECONDS);
        periodicTrigger.setInitialDelay(azeronClientProperties.getPingIntervalSeconds());
        this.pingSchedule = azeronTaskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                PongDto pongDto = pinger.ping();
                log.trace("Pong result -> "+pongDto);
                AzeronServerStatusTracker.Status status = pongDto.getStatus().equals(ResponseStatus.OK) ? AzeronServerStatusTracker.Status.UP : AzeronServerStatusTracker.Status.DOWN;
                azeronServerStatusTracker.setStatus(status);
                if(status.equals(AzeronServerStatusTracker.Status.UP) && pongDto.isAskedForDiscovery()){
                    if(pongDto.isDiscovered())
                        return;
                    eventListenerRegistry.reRegisterAll();
                }
            }
        }, periodicTrigger);
    }

    private void startUnseenRetrieveSchedule(){
        log.trace("Starting unseen retrieve task schedule");
        PeriodicTrigger periodicTrigger = new PeriodicTrigger(azeronClientProperties.getUnseenQueryIntervalSeconds(), TimeUnit.SECONDS);
        periodicTrigger.setInitialDelay(azeronClientProperties.getUnseenQueryIntervalSeconds());
        this.unseenSchedule = azeronTaskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                unseenRetrieveService.execute();
            }
        }, periodicTrigger);
    }

    @PreDestroy
    public void destroy(){
        pingSchedule.cancel(true);
        unseenSchedule.cancel(true);
    }
}