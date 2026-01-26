package com.example.webshopping.dto;

import com.example.webshopping.constant.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {

    // 배송 정보
    private String recipientName;
    private String recipientPhone;
    private String deliveryAddress;
    private String deliveryMessage;
    
    // 결제 정보
    private PaymentMethod paymentMethod;  // 결제 수단
    private String orderId;               // 주문 ID (UUID)
    private Integer amount;               // 결제 금액

}
