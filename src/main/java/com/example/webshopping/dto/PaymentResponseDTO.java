package com.example.webshopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 토스페이먼츠 결제 승인 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    
    private String paymentKey;
    private String orderId;
    private String status;              // DONE, CANCELED 등
    private Integer totalAmount;
    private String method;              // 카드, 계좌이체 등
    private LocalDateTime approvedAt;
    private String orderName;           // 주문명
    
    // 카드 결제일 경우
    private Card card;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Card {
        private String company;         // 카드사
        private String number;          // 카드번호 (일부 마스킹)
        private Integer installmentPlanMonths; // 할부 개월
    }
}
