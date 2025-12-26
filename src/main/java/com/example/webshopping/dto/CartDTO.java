package com.example.webshopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {
    private Long cartId;

    @Builder.Default
    private List<CartItemDTO> items = new ArrayList<>();

    private Integer totalPrice;
    private Integer totalQuantity;

    //전체 합계 금액 계산
    public Integer calculateTotalPrice() {
        return items.stream()
                .mapToInt(CartItemDTO::getTotalPrice)
                .sum();
    }

    //전체 수량 계싼
    public Integer calculateTotalQuantity() {
        return items.stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();
    }
}
