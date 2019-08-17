package io.pinect.azeron.client.service;

import io.pinect.azeron.client.domain.model.NatsConfigModel;
import io.pinect.azeron.client.domain.model.NatsConfigModelContainedEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


@Service
@Log4j2
public class AzeronNatsConfigChoserService implements NatsConfigChoserService{
    public NatsConfigModel getBestNatsConfig(List<? extends NatsConfigModelContainedEntity> list){
        InetAddress inetAddress;
        AtomicReference<NatsConfigModel> atomicReference = new AtomicReference<>(null);
        try {
            inetAddress = InetAddress.getLocalHost();
            list.forEach(o -> {
                if(o.getNats().getHostIp().equals(inetAddress.getHostAddress()) || o.getNats().getHost().equals(inetAddress.getHostName()))
                    atomicReference.set(o.getNats());
            });
        } catch (UnknownHostException e) {
            log.error(e);
        }

        if (atomicReference.get() != null) {
            return atomicReference.get();
        }

        return list.get(0).getNats();
    }
}
