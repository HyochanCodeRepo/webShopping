package com.example.webshopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토스페이먼츠 결제 승인 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {
    
    private String paymentKey;  // 토스에서 발급한 결제 키
    private String orderId;     // 주문 ID (UUID)
    private Integer amount;     // 결제 금액
}
