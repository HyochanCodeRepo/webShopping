package com.example.webshopping.service;

import com.example.webshopping.constant.OrderStatus;
import com.example.webshopping.dto.OrderItemDTO;
import com.example.webshopping.dto.OrderRequestDTO;
import com.example.webshopping.dto.OrderResponseDTO;
import com.example.webshopping.entity.*;
import com.example.webshopping.repository.CartRepository;
import com.example.webshopping.repository.MembersRepository;
import com.example.webshopping.repository.OrderRepository;
import com.example.webshopping.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MembersRepository membersRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;


    @Override
    public Long createOrder(String email, OrderRequestDTO orderRequestDTO) {

        // 1. 회원조회
        Members members =
            membersRepository.findByEmail(email);

        if (members == null) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다.");
        }

        // 2. 장바구니 조회
        Cart cart =
            cartRepository.findByMembers_Id(members.getId()).orElseThrow(() -> new EntityNotFoundException("장바구니가 비어있습니다."));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("장바구니에 상품이 없습니다.");
        }

        // 3. 주문 생성
        Order order = Order.createOrder(
                members,
                orderRequestDTO.getRecipientName(),
                orderRequestDTO.getRecipientPhone(),
                orderRequestDTO.getDeliveryAddress(),
                orderRequestDTO.getDeliveryMessage()
        );

        // 4. 장바구니 상품들을 주문 상품으로 변환
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException(
                        product.getProductName() + "의 재고가 부족합니다. (현재 재고 : "
                                + product.getStockQuantity() + "개)"
                );
            }

            // OrderItem 생성 (재고 차감 포함)
            OrderItem orderItem = OrderItem.createOrderItem(product, cartItem.getQuantity());
            order.addOrderItem(orderItem);

        }
        // 5. 총 금액 계산
        order.calculateTotalPrice();

        // 6. 주문 저장
        orderRepository.save(order);

        // 7. 장바구니 비우기
        cart.getCartItems().clear();
        cartRepository.save(cart);

        log.info("주문 생성 완료 - 주문번호 : {}, 회원 : {}", order.getId(), email);

        return order.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(Long orderId) {
        Order order =
            orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        return convertToDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyOrders(String email) {
        Members members =
                membersRepository.findByEmail(email);
        if (members == null) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다.");

        }
        List<Order> orders = orderRepository.findByMember_IdOrderByOrderDateDesc(members.getId());

        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order =
            orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        order.cancel();
        orderRepository.save(order);

        log.info("주문 취소 완료 - 주문번호: {}", orderId);

    }

    // Order 엔티티를 OrderResponseDTO로 변환
    public OrderResponseDTO convertToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .orderItemId(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getProductName())
                        .imageUrl(item.getProduct().getRepImageUrl())
                        .quantity(item.getQuantity())
                        .orderPrice(item.getOrderPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .orderStatusDescription(order.getOrderStatus().getDescription())
                .totalPrice(order.getTotalPrice())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryMessage(order.getDeliveryMessage())
                .items(itemDTOs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyProductOrders(String email) {
        List<Order> orders = orderRepository.findOrdersByProductOwnerEmail(email);

        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    //주문 상태 변경
    @Override
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        // 이미 취소되거나 배송완료된 주문은 상태 변경 불가
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("취소된 주문은 상태를 변경할 수 없습니다.");
        }

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송 완료된 주문은 상태를 변경할 수 없습니다.");
        }

        order.setOrderStatus(newStatus);
        orderRepository.save(order);

        log.info("주문 상태 변경 - 주문번호: {}, 새 상태: {}", orderId, newStatus);
    }

    /**
     * 내 상품의 진행 중인 주문 조회 (CONFIRMED, PREPARING, SHIPPED)
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyActiveOrders(String email) {
        log.info("======== getMyActiveOrders 호출 ========");
        log.info("검색 이메일: {}", email);
        
        List<Order> orders = orderRepository.findOrdersByProductOwnerEmail(email);
        log.info("Repository에서 조회된 주문 수: {}", orders.size());
        
        if (!orders.isEmpty()) {
            log.info("조회된 주문 목록:");
            orders.forEach(order -> 
                log.info("  - 주문번호: {}, 상태: {}", order.getId(), order.getOrderStatus())
            );
        }
        
        List<OrderResponseDTO> result = orders.stream()
                .filter(order ->
                        order.getOrderStatus() == OrderStatus.PENDING ||      // ← PENDING 추가!
                        order.getOrderStatus() == OrderStatus.CONFIRMED ||
                        order.getOrderStatus() == OrderStatus.PREPARING ||
                        order.getOrderStatus() == OrderStatus.SHIPPED
                )
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        log.info("필터링 후 진행중 주문 수: {}", result.size());
        log.info("======================================");
        
        return result;
    }

    /**
     * 내 상품의 완료된 주문 조회 (DELIVERED, CANCELLED)
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyCompletedOrders(String email) {
        List<Order> orders = orderRepository.findOrdersByProductOwnerEmail(email);

        return orders.stream()
                .filter(order ->
                        order.getOrderStatus() == OrderStatus.DELIVERED ||
                                order.getOrderStatus() == OrderStatus.CANCELLED
                )
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
