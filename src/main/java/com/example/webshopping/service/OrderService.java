package com.example.webshopping.service;

import com.example.webshopping.constant.OrderStatus;
import com.example.webshopping.dto.OrderRequestDTO;
import com.example.webshopping.dto.OrderResponseDTO;

import java.util.List;

public interface OrderService {

    //주문 생성
    Long createOrder(String email, OrderRequestDTO orderRequestDTO);

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

}
