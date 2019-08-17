package io.pinect.azeron.client.service.stateListener;

import lombok.extern.log4j.Log4j2;
import nats.client.ConnectionStateListener;
import nats.client.Nats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("natsConnectionStateListener")
@Log4j2
public class AzeronNatsConnecionStateListener implements NatsConnectionStateListener{
    private ConnectionStateListener.State state;

    @Autowired
    public AzeronNatsConnecionStateListener() {}

    @Override
    public ConnectionStateListener.State getCurrentState() {
        return this.state;
    }

    @Override
    public void onConnectionStateChange(Nats nats, ConnectionStateListener.State state) {
        log.info("Nats state changed from "+ this.state + " to "+ state);

        switch (state){
            case CONNECTED:
                break;
        }

        this.state = state;
    }
}
