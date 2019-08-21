package io.pinect.azeron.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.config.ChannelName;
import io.pinect.azeron.client.config.properties.AzeronClientProperties;
import io.pinect.azeron.client.domain.dto.out.SubscriptionControlDto;
import io.pinect.azeron.client.domain.dto.out.UnSubscribeControlDto;
import io.pinect.azeron.client.service.handler.EventListener;
import lombok.extern.log4j.Log4j2;
import nats.client.Message;
import nats.client.MessageHandler;
import nats.client.Nats;
import nats.client.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.pinect.azeron.client.config.ChannelName.AZERON_SUBSCRIBE_API_NAME;
import static io.pinect.azeron.client.config.ChannelName.AZERON_UNSUBSCRIBE_API_NAME;


@Log4j2
@Service
public class EventListenerRegistry {
    private volatile Map<String, EventListener> eventListenersMap = new ConcurrentHashMap<>();
    private volatile Map<String, Subscription> subscriptionMap = new ConcurrentHashMap<>();
    private final AtomicReference<Nats> natsAtomicReference;
    private final ObjectMapper objectMapper;
    private final AzeronClientProperties azeronClientProperties;
    private final String serviceName;

    @Autowired
    public EventListenerRegistry(AtomicNatsHolder atomicNatsHolder, ObjectMapper objectMapper, AzeronClientProperties azeronClientProperties, @Value("${spring.application.name}") String serviceName) {
        this.natsAtomicReference = atomicNatsHolder.getNatsAtomicReference();
        this.objectMapper = objectMapper;
        this.azeronClientProperties = azeronClientProperties;
        this.serviceName = serviceName;
    }

    public void register(EventListener eventListener) {
        log.debug("Registering event listener -> event name: "+ eventListener.eventName() + " |  service : " + eventListener.clientConfig().getServiceName());
        eventListenersMap.put(eventListener.eventName(), eventListener);
        try {
            subscribe(eventListener);
        } catch (JsonProcessingException e) {
            eventListenersMap.remove(eventListener.eventName());
            throw new RuntimeException(e);
        }
    }

    public void reRegisterAll(){
        log.info("Re-registering all channels into Azeron.");
        for(String channelName: eventListenersMap.keySet()){
            try {
                EventListener eventListener = eventListenersMap.get(channelName);
                subscribe(eventListener);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void drop(String channelName){
        log.debug("Dropping channel: "+ channelName);

        try {
            Subscription subscription = subscriptionMap.get(channelName);
            if(subscription != null) {
                subscription.close();
            }
        }catch (Exception e){
            log.error(e);
        }

        try {
            natsAtomicReference.get().publish(AZERON_UNSUBSCRIBE_API_NAME, objectMapper.writeValueAsString(
                    new UnSubscribeControlDto(channelName, serviceName)
            ));
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
        }
        eventListenersMap.remove(channelName);
    }

    public EventListener getEventListenerOfChannel(String channelName){
        return eventListenersMap.get(channelName);
    }

    private void subscribe(EventListener eventListener) throws JsonProcessingException {
        Subscription subscription = subscriptionMap.get(eventListener.eventName());
        if(eventListener.useAzeron())
            subscribeWithAzeron(eventListener);
        else
            subscribeToNats(eventListener);
        if(subscription != null)
            subscription.close();
    }

    private void subscribeWithAzeron(EventListener eventListener) throws JsonProcessingException {
        SubscriptionControlDto subscriptionControlDto = getSubscriptionControlDto(eventListener);
        String json = getSubscriptionControlJson(subscriptionControlDto);
        Nats nats = natsAtomicReference.get();
        if(nats.isConnected()){
            nats.request(AZERON_SUBSCRIBE_API_NAME, json, 5, TimeUnit.SECONDS, new MessageHandler() {
                @Override
                public void onMessage(Message message) {
                    if(message.getBody().equals("OK")){
                        try {
                            subscribeToNats(eventListener);
                        } catch (Exception e) {
                            log.error("Could not subscribe to nats", e);
                            throw new RuntimeException(e);
                        }
                    }else{
                        throw new RuntimeException("Azeron <server> failed to subscribe to "+eventListener.eventName());
                    }
                }
            });
        }
    }

    private void subscribeToNats(EventListener eventListener) {
        Nats nats = natsAtomicReference.get();
        if(nats.isConnected()){
            Subscription subscribe = null;
            if(eventListener.clientConfig().isUseQueueGroup())
                subscribe = getQueueGroupSubscription(nats, eventListener);
            else
                subscribe = getSingleServiceSubsription(nats, eventListener);
            subscriptionMap.put(eventListener.eventName(), subscribe);
        }else {
            throw new RuntimeException("Nats is not connected.");
        }
    }

    private Subscription getSingleServiceSubsription(Nats nats, EventListener eventListener) {
        return nats.subscribe(eventListener.eventName(), new MessageHandler() {
            @Override
            public void onMessage(Message message) {
                processMessage(message, eventListener);
            }
        });
    }

    private Subscription getQueueGroupSubscription(Nats nats, EventListener eventListener) {
        return nats.subscribe(eventListener.eventName(), eventListener.clientConfig().getServiceName(), new MessageHandler() {
            @Override
            public void onMessage(Message message) {
                processMessage(message, eventListener);
            }
        });
    }

    private void processMessage(Message message, EventListener eventListener) {
        log.debug("Processing message -> event name: "+ eventListener.eventName() + " | message: "+ message.getBody());
        eventListener.onMessage(message);
    }

    private String getSubscriptionControlJson(SubscriptionControlDto subscriptionControlDto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(subscriptionControlDto);
    }

    private SubscriptionControlDto getSubscriptionControlDto(EventListener eventListener) {
        return new SubscriptionControlDto(eventListener.eventName(), eventListener.clientConfig());
    }

    @PreDestroy
    public void preDestroy(){
        for(String key:subscriptionMap.keySet()){
            Subscription subscription = subscriptionMap.get(key);
            subscription.close();
        }

        if(azeronClientProperties.isUnSubscribeWhenShuttingDown()){
            log.debug("UnSubscribe for shutdown");
            Nats nats = natsAtomicReference.get();
            for(String channelName: eventListenersMap.keySet()){
                drop(channelName);
            }
        }
    }
}