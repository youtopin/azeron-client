package io.pinect.azeron.client.domain.repository;

import io.pinect.azeron.client.service.publisher.EventMessagePublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Fallback repository used to handle retry to send messages when they are failed
 */
public interface FallbackRepository {
    void saveMessage(FallbackEntity fallbackEntity);
    void deleteMessage(String id);
    List<FallbackEntity> getMessages(int offset, int limit);
    int countAll();

    /**
     * Default implementation fallback repository
     */
    class VoidFallbackRepository implements FallbackRepository {

        @Override
        public void saveMessage(FallbackEntity fallbackEntity) {
        }

        @Override
        public void deleteMessage(String id) {
        }

        @Override
        public List<FallbackEntity> getMessages(int offset, int limit) {
            return new ArrayList<>();
        }

        @Override
        public int countAll() {
            return 0;
        }
    }

    /**
     * Fallback entity to persist and used when retrying to send message later
     */
    class FallbackEntity {
        private String id;
        private String message;
        private String eventName;
        private EventMessagePublisher.PublishStrategy strategy;

        public FallbackEntity() {
        }

        public FallbackEntity(String id, String message, String eventName, EventMessagePublisher.PublishStrategy publishStrategy) {
            this.id = id;
            this.message = message;
            this.eventName = eventName;
            this.strategy = publishStrategy;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public EventMessagePublisher.PublishStrategy getStrategy() {
            return strategy;
        }

        public void setStrategy(EventMessagePublisher.PublishStrategy strategy) {
            this.strategy = strategy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FallbackEntity that = (FallbackEntity) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
