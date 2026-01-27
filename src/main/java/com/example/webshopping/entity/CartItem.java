package com.example.webshopping.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 선택한 옵션 (null이면 옵션 없는 상품)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
    
    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public static CartItem createCartItem(Cart cart, Product product, ProductOption productOption, Integer quantity) {
        return CartItem.builder()
                .cart(cart)
                .product(product)
                .productOption(productOption)
                .quantity(quantity)
                .createdDate(LocalDateTime.now())
                .build();
    }
}
