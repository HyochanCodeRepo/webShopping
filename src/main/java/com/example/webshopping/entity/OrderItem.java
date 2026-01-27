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

    // 선택한 옵션 (null이면 옵션 없는 상품)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer orderPrice;

    //상품별 총액계산
    public Integer getTotalPrice() {
        return orderPrice * quantity;
    }

    //정적 팩토리 메서드
    public static OrderItem createOrderItem(Product product, ProductOption productOption, Integer quantity) {
        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .productOption(productOption)
                .quantity(quantity)
                .orderPrice(product.getDiscountPrice() + (productOption != null ? productOption.getAdditionalPrice() : 0))
                .build();

        // 재고 차감 - 옵션이 있으면 옵션 재고, 없으면 상품 재고
        if (productOption != null) {
            productOption.setStockQuantity(productOption.getStockQuantity() - quantity);
        } else {
            product.setStockQuantity(product.getStockQuantity() - quantity);
        }

        return orderItem;
    }

    //주문 취소 시 재고 복구
    public void cancel(){
        if (this.productOption != null) {
            this.productOption.setStockQuantity(this.productOption.getStockQuantity() + this.quantity);
        } else {
            this.product.setStockQuantity(this.product.getStockQuantity() + this.quantity);
        }
    }
}
