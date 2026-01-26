package com.example.webshopping.service;

import com.example.webshopping.dto.PaymentRequestDTO;
import com.example.webshopping.dto.PaymentResponseDTO;

/**
 * 결제 서비스 인터페이스
 */
public interface PaymentService {
    
    /**
     * 토스페이먼츠 결제 승인
     * @param paymentRequestDTO 결제 승인 요청 정보
     * @return 결제 승인 결과
     */
    PaymentResponseDTO confirmPayment(PaymentRequestDTO paymentRequestDTO);
    
    /**
     * 결제 취소
     * @param paymentKey 토스 결제 키
     * @param cancelReason 취소 사유
     * @return 취소 결과
     */
    PaymentResponseDTO cancelPayment(String paymentKey, String cancelReason);
}
