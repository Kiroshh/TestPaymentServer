package com.kirosh.stripetestapp.dto;

public class CancelSubscriptionRequest {
    private String subscriptionId;

    public CancelSubscriptionRequest() {
    }

    public CancelSubscriptionRequest(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}
