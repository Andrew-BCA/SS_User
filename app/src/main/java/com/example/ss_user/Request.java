package com.example.ss_user;

public class Request {
    private String reason;
    private String requestedDate;
    private String status;
    private long timestamp;
    private String userId;

    // Add empty constructor and getters/setters
    public Request() {}

    public String getReason() { return reason; }
    public String getRequestedDate() { return requestedDate; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }
}