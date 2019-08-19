package io.pinect.azeron.client.domain.dto.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.pinect.azeron.client.domain.dto.ResponseStatus;
import io.pinect.azeron.client.domain.model.NatsConfigModel;
import io.pinect.azeron.client.domain.model.NatsConfigModelContainedEntity;
import lombok.*;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class InfoResultDto extends BasicAzeronReponseDto {
    private List<InfoResult> results;

    public InfoResultDto(){
        super(ResponseStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InfoResult implements NatsConfigModelContainedEntity {
        private String serverUUID;
        private NatsConfigModel nats;
        private int channelsCount;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InfoResult)) return false;
            InfoResult that = (InfoResult) o;
            return Objects.equals(getServerUUID(), that.getServerUUID());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getServerUUID());
        }
    }
}
