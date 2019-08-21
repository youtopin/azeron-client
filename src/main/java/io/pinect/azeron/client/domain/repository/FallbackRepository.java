package io.pinect.azeron.client.domain.repository;

import io.pinect.azeron.client.service.publisher.EventMessagePublisher;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Fallback repository used to handle retry to send messages when they are failed
 */
public interface FallbackRepository {
    void saveMessage(FallbackEntity fallbackEntity);
    void deleteMessage(String id);
    void deleteMessage(FallbackEntity fallbackEntity);
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
        public void deleteMessage(FallbackEntity fallbackEntity) {

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
    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    class FallbackEntity {
        private String id;
        private String message;
        private String eventName;
        private EventMessagePublisher.PublishStrategy strategy;

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
