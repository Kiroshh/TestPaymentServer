package com.kirosh.stripetestapp.dto;

public class UpdateSubscriptionRequest {
    private String subscriptionId;
    private String newPriceId;

    public UpdateSubscriptionRequest() {
    }

    public UpdateSubscriptionRequest(String subscriptionId, String newPriceId) {
        this.subscriptionId = subscriptionId;
        this.newPriceId = newPriceId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getNewPriceId() {
        return newPriceId;
    }

    public void setNewPriceId(String newPriceId) {
        this.newPriceId = newPriceId;
    }
}
