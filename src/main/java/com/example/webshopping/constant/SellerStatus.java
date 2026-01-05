package com.example.webshopping.constant;

public enum SellerStatus {
    PENDING("승인 대기"),
    APPROVED("승인 완료"),
    REJECTED("승인 거부");

    private final String description;

    SellerStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}