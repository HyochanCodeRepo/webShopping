package com.example.webshopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartRequestDTO {

    private Long productId;
    private Long productOptionId;  // 선택한 옵션 ID (null이면 옵션 없는 상품)
    private Integer quantity;
}
