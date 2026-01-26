package com.example.webshopping.service;

import com.example.webshopping.dto.PaymentRequestDTO;
import com.example.webshopping.dto.PaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 토스페이먼츠 결제 서비스 구현
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String TOSS_CANCEL_URL = "https://api.tosspayments.com/v1/payments/";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public PaymentResponseDTO confirmPayment(PaymentRequestDTO paymentRequestDTO) {
        log.info("✅ 토스페이먼츠 결제 승인 시작 - orderId: {}, amount: {}", 
                 paymentRequestDTO.getOrderId(), paymentRequestDTO.getAmount());

        try {
            // HTTP 헤더 설정 (Basic Auth)
            HttpHeaders headers = createHeaders();

            // 요청 바디 설정
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("paymentKey", paymentRequestDTO.getPaymentKey());
            requestBody.put("orderId", paymentRequestDTO.getOrderId());
            requestBody.put("amount", paymentRequestDTO.getAmount());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 토스페이먼츠 API 호출
            ResponseEntity<PaymentResponseDTO> response = restTemplate.exchange(
                    TOSS_CONFIRM_URL,
                    HttpMethod.POST,
                    entity,
                    PaymentResponseDTO.class
            );

            PaymentResponseDTO paymentResponse = response.getBody();
            log.info("✅ 토스페이먼츠 결제 승인 완료 - paymentKey: {}, status: {}", 
                     paymentResponse.getPaymentKey(), paymentResponse.getStatus());

            return paymentResponse;

        } catch (Exception e) {
            log.error("❌ 토스페이먼츠 결제 승인 실패: {}", e.getMessage(), e);
            throw new RuntimeException("결제 승인에 실패했습니다: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponseDTO cancelPayment(String paymentKey, String cancelReason) {
        log.info("✅ 토스페이먼츠 결제 취소 시작 - paymentKey: {}", paymentKey);

        try {
            // HTTP 헤더 설정
            HttpHeaders headers = createHeaders();

            // 요청 바디 설정
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("cancelReason", cancelReason);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 토스페이먼츠 API 호출
            String cancelUrl = TOSS_CANCEL_URL + paymentKey + "/cancel";
            ResponseEntity<PaymentResponseDTO> response = restTemplate.exchange(
                    cancelUrl,
                    HttpMethod.POST,
                    entity,
                    PaymentResponseDTO.class
            );

            PaymentResponseDTO paymentResponse = response.getBody();
            log.info("✅ 토스페이먼츠 결제 취소 완료 - paymentKey: {}", paymentKey);

            return paymentResponse;

        } catch (Exception e) {
            log.error("❌ 토스페이먼츠 결제 취소 실패: {}", e.getMessage(), e);
            throw new RuntimeException("결제 취소에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * HTTP 헤더 생성 (Basic Auth)
     * 토스페이먼츠는 Secret Key를 Base64 인코딩하여 Authorization 헤더에 넣음
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Secret Key를 Base64로 인코딩
        String encodedAuth = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        return headers;
    }
}
