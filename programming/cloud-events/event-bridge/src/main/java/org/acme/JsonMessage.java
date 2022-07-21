package org.acme;

public class JsonMessage {
    String message;
    String topic;

    public JsonMessage() {
    }

    public JsonMessage(String message, String topic) {
        this.message = message;
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
