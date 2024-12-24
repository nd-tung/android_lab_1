package com.socket.model;

import java.io.Serializable;
import java.sql.Timestamp;
// Convert object to byte array to send over network
public class MessageObject implements Serializable {
    private String message;
    private Long timestamp;

    private String source;


    public MessageObject(String message, Long timestamp, String source) {
        this.message = message;
        this.timestamp = timestamp;
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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
