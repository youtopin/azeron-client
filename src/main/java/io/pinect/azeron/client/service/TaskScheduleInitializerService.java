package io.pinect.azeron.client.service;

import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.config.properties.AzeronClientProperties;
import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.dto.ResponseStatus;
import io.pinect.azeron.client.domain.dto.in.PongDto;
import io.pinect.azeron.client.service.api.NatsConfigProvider;
import io.pinect.azeron.client.service.api.Pinger;
import io.pinect.azeron.client.service.api.UnseenRetrieveQueryService;
import io.pinect.azeron.client.service.handler.EventListener;
import io.pinect.azeron.client.service.publisher.FallbackPublisherService;
import io.pinect.azeron.client.service.stateListener.NatsConnectionStateListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class TaskScheduleInitializerService {
    private final AzeronClientProperties azeronClientProperties;
    private final EventListenerRegistry eventListenerRegistry;
    private final AzeronServerStatusTracker azeronServerStatusTracker;
    private final UnseenRetrieveQueryService unseenRetrieveQueryService;
    private final FallbackPublisherService fallbackPublisherService;
    private final TaskScheduler azeronTaskScheduler;
    private final NatsReconnectForceService natsReconnectForceService;
    private ScheduledFuture<?> pingSchedule;
    private ScheduledFuture<?> unseenSchedule;
    private ScheduledFuture<?> fallbackPublishSchedule;
    private ScheduledFuture<?> natsConnectionCheckSchedule;
    private final Pinger pinger;

    @Autowired
    public TaskScheduleInitializerService(AzeronClientProperties azeronClientProperties, EventListenerRegistry eventListenerRegistry, AzeronServerStatusTracker azeronServerStatusTracker, UnseenRetrieveQueryService unseenRetrieveQueryService, FallbackPublisherService fallbackPublisherService, AtomicNatsHolder atomicNatsHolder, TaskScheduler azeronTaskScheduler, NatsConnectionStateListener natsConnectionStateListener, NatsConfigProvider natsConfigProvider, ApplicationContext applicationContext, NatsReconnectForceService natsReconnectForceService, Pinger pinger) {
        this.azeronClientProperties = azeronClientProperties;
        this.eventListenerRegistry = eventListenerRegistry;
        this.azeronServerStatusTracker = azeronServerStatusTracker;
        this.unseenRetrieveQueryService = unseenRetrieveQueryService;
        this.fallbackPublisherService = fallbackPublisherService;
        this.azeronTaskScheduler = azeronTaskScheduler;
        this.natsReconnectForceService = natsReconnectForceService;
        this.pinger = pinger;
    }

    public void initialize() {
        log.info("Initializing scheduled tasks");
        startPingTaskSchedule();
        startUnseenRetrieveSchedule();
        startFallbackPublishSchedule();
    }

    private void startPingTaskSchedule(){
        log.info("Starting ping task schedule");
        PeriodicTrigger periodicTrigger = new PeriodicTrigger(azeronClientProperties.getPingIntervalSeconds(), TimeUnit.SECONDS);
        periodicTrigger.setInitialDelay(azeronClientProperties.getPingIntervalSeconds());
        this.pingSchedule = azeronTaskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                PongDto pongDto = pinger.ping();
                log.debug("Pong result -> "+pongDto);
                AzeronServerStatusTracker.Status status = pongDto.getStatus().equals(ResponseStatus.OK) ? AzeronServerStatusTracker.Status.UP : AzeronServerStatusTracker.Status.DOWN;
                azeronServerStatusTracker.setStatus(status);
                if(status.equals(AzeronServerStatusTracker.Status.UP) && pongDto.isAskedForDiscovery()){
                    if(pongDto.isDiscovered())
                        return;
                    reRegisterIfNeeded();
                }
            }
        }, periodicTrigger);
    }

    private void reRegisterIfNeeded() {
        boolean b = false;
        for (EventListener eventListener : eventListenerRegistry.getEventListeners()) {
            if(!eventListener.policy().equals(HandlerPolicy.NO_AZERON)){
                b = true;
                break;
            }
        }
        if(b)
            eventListenerRegistry.reRegisterAll();
    }

    private void startUnseenRetrieveSchedule(){
        if(!azeronClientProperties.isRetrieveUnseen())
            return;
        log.info("Starting unseen retrieve task schedule");
        PeriodicTrigger periodicTrigger = new PeriodicTrigger(azeronClientProperties.getUnseenQueryIntervalSeconds(), TimeUnit.SECONDS);
        periodicTrigger.setInitialDelay(azeronClientProperties.getUnseenQueryIntervalSeconds());
        this.unseenSchedule = azeronTaskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                unseenRetrieveQueryService.execute();
            }
        }, periodicTrigger);
    }

    private void startFallbackPublishSchedule(){
        log.info("Starting fallback publish task schedule");
        PeriodicTrigger periodicTrigger = new PeriodicTrigger(azeronClientProperties.getFallbackPublishIntervalSeconds(), TimeUnit.SECONDS);
        periodicTrigger.setInitialDelay(azeronClientProperties.getFallbackPublishIntervalSeconds());
        this.fallbackPublishSchedule = azeronTaskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                fallbackPublisherService.execute();
            }
        }, periodicTrigger);
    }

    private void startConnectionCheckSchedule(){
        log.info("starting connection check schedule");
        PeriodicTrigger periodicTrigger = new PeriodicTrigger(10, TimeUnit.SECONDS);
        periodicTrigger.setInitialDelay(10000);
        this.natsConnectionCheckSchedule = azeronTaskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                natsReconnectForceService.forceNatsReconnect();
            }
        }, periodicTrigger);
    }

    @PreDestroy
    public void destroy(){
        if(pingSchedule != null)
            pingSchedule.cancel(true);
        if(unseenSchedule != null)
            unseenSchedule.cancel(true);
        if(fallbackPublishSchedule != null)
            fallbackPublishSchedule.cancel(true);
        if(natsConnectionCheckSchedule != null)
            natsConnectionCheckSchedule.cancel(true);
    }
}