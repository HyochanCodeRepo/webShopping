package com.example.webshopping.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionDTO {
    private Long id;
    private Long productId;
    private String optionType;       // "사이즈", "색상"
    private String optionValue;      // "M", "250", "블랙"
    private Integer stockQuantity;   // 재고 수량
    private Integer additionalPrice; // 옵션 추가 금액
    private Integer displayOrder;    // 정렬 순서
    private Boolean isActive;        // 사용 여부
}
