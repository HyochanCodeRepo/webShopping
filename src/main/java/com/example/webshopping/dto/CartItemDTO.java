package com.example.webshopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {

    private Long cartItemId;
    private Long productId;
    private String productName;
    private Integer price;
    private Integer discountRate;
    private Integer discountPrice;
    private Integer quantity;
    private String imageUrl;
    private Integer stockQuantity;

    public Integer getTotalPrice(){
        return discountPrice * quantity;
    }
}
