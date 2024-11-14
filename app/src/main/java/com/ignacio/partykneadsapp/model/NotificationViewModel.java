package com.ignacio.partykneadsapp.model;

public class NotificationViewModel {
    private String orderStatus;
    private String userRateComment;
    private String cakeImageUrl;  // Field for cake image URL

    // Constructor
    public NotificationViewModel(String orderStatus, String userRateComment, String cakeImageUrl) {
        this.orderStatus = orderStatus;
        this.userRateComment = userRateComment;
        this.cakeImageUrl = cakeImageUrl != null ? cakeImageUrl : "";
    }

    // Getters and Setters
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getUserRateComment() {
        return userRateComment;
    }

    public void setUserRateComment(String userRateComment) {
        this.userRateComment = userRateComment;
    }

    public String getCakeImageUrl() {
        return cakeImageUrl;
    }

    public void setCakeImageUrl(String cakeImageUrl) {
        this.cakeImageUrl = cakeImageUrl;
    }
}