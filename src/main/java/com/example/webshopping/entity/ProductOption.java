package com.example.webshopping.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString(exclude = "product")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 옵션 타입 (예: "사이즈", "색상")
    @Column(nullable = false, length = 50)
    private String optionType;

    // 옵션 값 (예: "M", "250", "블랙")
    @Column(nullable = false, length = 50)
    private String optionValue;

    // 재고 수량
    @Column(nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    // 옵션 추가 금액 (기본 0원)
    @Builder.Default
    private Integer additionalPrice = 0;

    // 정렬 순서
    @Builder.Default
    private Integer displayOrder = 0;

    // 사용 여부
    @Builder.Default
    private Boolean isActive = true;
}
