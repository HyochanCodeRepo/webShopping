package com.example.webshopping.constant;

/**
 * 결제 상태
 */
public enum PaymentStatus {
    READY("결제 대기"),           // 주문 생성, 결제 전
    IN_PROGRESS("결제 진행중"),    // 결제창 호출됨
    DONE("결제 완료"),            // 결제 성공
    CANCELED("결제 취소"),         // 사용자가 취소
    FAILED("결제 실패");           // 결제 실패

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
