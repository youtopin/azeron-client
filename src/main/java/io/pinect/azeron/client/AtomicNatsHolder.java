package io.pinect.azeron.client;

import lombok.Getter;
import nats.client.Nats;

import java.util.concurrent.atomic.AtomicReference;

@Getter
public class AtomicNatsHolder {
    private final AtomicReference<Nats> natsAtomicReference;

    public AtomicNatsHolder(AtomicReference<Nats> natsAtomicReference) {
        this.natsAtomicReference = natsAtomicReference;
    }

    public AtomicNatsHolder(Nats nats){
        this.natsAtomicReference = new AtomicReference<>(nats);
    }
}
