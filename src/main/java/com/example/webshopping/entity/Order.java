package com.example.webshopping.entity;

import com.example.webshopping.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Members member;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private Integer totalPrice;

    //배송정보
    @Column(nullable = false)
    private String recipientName; //받는 사람

    @Column(nullable = false)
    private String recipientPhone; //전화번호

    @Column(nullable = false, length = 500)
    private String deliveryAddress; //배송지

    @Column(length = 500)
    private String deliveryMessage; //배송 메시지

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
        this.orderStatus = OrderStatus.PENDING;
    }

    //양방향 연관관계 편의 메서드
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    //총 금액 계산
    public void calculateTotalPrice() {
        this.totalPrice = orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }

    // 정적 팩토리 메서드
    public static Order createOrder(Members member, String recipientName,
                                    String recipientPhone, String deliveryAddress,
                                    String deliveryMessage) {
        return Order.builder()
                .member(member)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .deliveryAddress(deliveryAddress)
                .deliveryMessage(deliveryMessage)
                .orderItems(new ArrayList<>())
                .build();
    }

    // 주문 취소
    public void cancel() {
        if (this.orderStatus == OrderStatus.SHIPPED ||
                this.orderStatus == OrderStatus.DELIVERED) {
            throw new IllegalStateException("이미 배송중이거나 배송완료된 주문은 취소할 수 없습니다.");
        }
        this.orderStatus = OrderStatus.CANCELLED;

        // 재고 복구
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

}
