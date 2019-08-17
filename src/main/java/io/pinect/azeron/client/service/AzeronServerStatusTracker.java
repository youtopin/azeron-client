package io.pinect.azeron.client.service;

public interface AzeronServerStatusTracker {
    default boolean isUp(){
        return getStatus().equals(Status.UP);
    }
    default boolean isDown(){
        return getStatus().equals(Status.DOWN);
    }

    void setStatus(Status status);

    Status getStatus();

    public enum Status {
        UP, DOWN
    }
}
