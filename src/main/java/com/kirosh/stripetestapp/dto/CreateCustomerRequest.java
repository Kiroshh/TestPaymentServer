package com.kirosh.stripetestapp.dto;

public class CreateCustomerRequest {
    private String email;

    public CreateCustomerRequest() {
    }

    public CreateCustomerRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
