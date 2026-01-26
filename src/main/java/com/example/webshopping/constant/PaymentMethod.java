package com.example.webshopping.constant;

/**
 * 결제 수단
 */
public enum PaymentMethod {
    CARD("카드"),
    TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE("휴대폰"),
    KAKAO_PAY("카카오페이"),
    TOSS_PAY("토스페이"),
    NAVER_PAY("네이버페이");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
