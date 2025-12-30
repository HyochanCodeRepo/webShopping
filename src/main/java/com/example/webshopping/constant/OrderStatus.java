package com.example.webshopping.constant;


public enum OrderStatus {
    PENDING("결제 대기"),
    CONFIRMED("주문 확정"),
    PREPARING("배송 준비중"),
    SHIPPED("배송중"),
    DELIVERED("배송 완료"),
    CANCELLED("주문 취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
