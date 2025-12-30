package com.example.webshopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Log4j2
public class OrderItemDTO {

    private Long orderItemId;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Integer quantity;
    private Integer orderPrice;

    //상품별 총액 계산
    public Integer getTotalPrice() {
        return orderPrice * quantity;
    }
}
