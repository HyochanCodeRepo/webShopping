package com.example.webshopping.dto;

import com.example.webshopping.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Log4j2
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

    @Builder.Default
    private List<OrderItemDTO> items = new ArrayList<>();
}
