package com.example.webshopping.service;

import com.example.webshopping.constant.OrderStatus;
import com.example.webshopping.dto.OrderRequestDTO;
import com.example.webshopping.dto.OrderResponseDTO;
import com.example.webshopping.dto.PaymentRequestDTO;

import java.util.List;

public interface OrderService {

    //주문 생성 (일반 주문 - 결제 없음)
    Long createOrder(String email, OrderRequestDTO orderRequestDTO);
    
    //주문 생성 (토스 결제용 - 결제 전 주문 생성)
    Long createOrderForPayment(String email, OrderRequestDTO orderRequestDTO);
    
    //결제 완료 후 주문 확정
    void confirmOrderPayment(PaymentRequestDTO paymentRequestDTO);

    //주문 조회(단건)
    OrderResponseDTO getOrder(Long orderId);

    //내 주문 목록 조회
    List<OrderResponseDTO> getMyOrders(String email);

    //주문 취소
    void cancelOrder(Long orderId);

    List<OrderResponseDTO> getMyProductOrders(String email);

    void updateOrderStatus(Long orderId, OrderStatus newStatus);

    List<OrderResponseDTO> getMyActiveOrders(String email);

    List<OrderResponseDTO> getMyCompletedOrders(String email);
    
    // Order 엔티티를 DTO로 변환
    OrderResponseDTO convertToDTO(com.example.webshopping.entity.Order order);
    
    // 특정 시간 이후 상태 변경된 주문 건수 조회
    Long countUpdatedOrders(Long memberId, java.time.LocalDateTime lastCheckedTime);

}
