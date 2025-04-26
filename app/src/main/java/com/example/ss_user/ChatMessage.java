package com.example.ss_user;

public class ChatMessage {
    private String id;
    private String message;
    private String sender;
    private long timestamp;

    // Empty constructor (IMPORTANT for Firebase)
    public ChatMessage() {
    }

    public ChatMessage(String id, String message, String sender, long timestamp) {
        this.id = id;
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
