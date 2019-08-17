package io.pinect.azeron.client.exception;

public class AzeronIsDownException extends RuntimeException {
    public AzeronIsDownException() {
        super("Azeron server is down. wait for it to come up and redo!");
    }
}
