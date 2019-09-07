package io.pinect.azeron.client.exception;

public class PublishException extends Exception {
    private String serviceName;
    private String messageText;

    public PublishException(String serviceName, String messageText) {
        super("Failed to publish message to " + serviceName + " | text -> "+ messageText);
        this.serviceName = serviceName;
        this.messageText = messageText;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
