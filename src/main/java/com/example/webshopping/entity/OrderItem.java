package com.example.webshopping.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer orderPrice;

    //상품별 총액계산
    public Integer getTotalPrice() {
        return orderPrice * quantity;
    }

    //정적 팩토리 메서드
    public static OrderItem createOrderItem(Product product, Integer quantity) {
        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .orderPrice(product.getDiscountPrice())
                .build();

        //재고 차감
        product.setStockQuantity(product.getStockQuantity() - quantity);

        return orderItem;
    }

    //주문 취소 시 재고 복구
    public void cancel(){
        this.product.setStockQuantity(this.product.getStockQuantity() + this.quantity);
    }
}
