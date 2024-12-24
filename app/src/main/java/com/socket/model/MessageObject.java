package com.socket.model;

import java.io.Serializable;
import java.sql.Timestamp;
// Convert object to byte array to send over network
public class MessageObject implements Serializable {
    private String message;
    private Long timestamp;

    public MessageObject(String message, Long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
