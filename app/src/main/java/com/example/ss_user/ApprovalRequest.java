package com.example.ss_user;
public class ApprovalRequest {
    public String userId, userType, requestedDate, reason, status;
    public long timestamp;

    public ApprovalRequest() {
        // Needed for Firebase
    }

    public ApprovalRequest(String userId, String userType, String requestedDate, String reason, String status, long timestamp) {
        this.userId = userId;
        this.userType = userType;
        this.requestedDate = requestedDate;
        this.reason = reason;
        this.status = status;
        this.timestamp = timestamp;
    }
}
