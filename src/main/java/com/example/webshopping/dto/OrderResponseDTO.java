package com.example.webshopping.dto;

import com.example.webshopping.constant.OrderStatus;
import com.example.webshopping.constant.PaymentMethod;
import com.example.webshopping.constant.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {

    private Long orderId;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private String orderStatusDescription;
    private Integer totalPrice;

    // 배송 정보
    private String recipientName;
    private String recipientPhone;
    private String deliveryAddress;
    private String deliveryMessage;
    
    // 결제 정보
    private PaymentMethod paymentMethod;
    private String paymentMethodDescription;
    private PaymentStatus paymentStatus;
    private String paymentStatusDescription;
    private Integer paymentAmount;
    private LocalDateTime paymentApprovedAt;
    private String orderIdForPayment; // 토스용 주문 ID

    @Builder.Default
    private List<OrderItemDTO> items = new ArrayList<>();
}
