package io.pinect.azeron.client.util;

import io.pinect.azeron.client.domain.dto.in.InfoResultDto;
import io.pinect.azeron.client.domain.model.NatsConfigModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NatsConfigurationMerge {
    public static NatsConfigModel getMergedNatsConfig(List<InfoResultDto.InfoResult> infoResultList){
        Set<String> hostsSet = new HashSet<>();
        infoResultList.forEach(infoResult -> {
            hostsSet.addAll(infoResult.getNats().getHosts());
        });

        NatsConfigModel natsConfigModel = infoResultList.get(0).getNats();
        natsConfigModel.setHosts(new ArrayList<>(hostsSet));

        return natsConfigModel;
    }
}
