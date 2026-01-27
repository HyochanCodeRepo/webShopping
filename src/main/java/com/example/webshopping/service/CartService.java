package com.example.webshopping.service;

import com.example.webshopping.dto.CartDTO;
import com.example.webshopping.entity.Cart;

public interface CartService {

    public void addCart(String email, Long productId, Long productOptionId, Integer quantity);

    Cart getCart(String email);

    int getCartItemCount(String email);

    /*사용자 장바구니 조회*/
    CartDTO getCartDTO(String email);

    /*장바구니 아이템 수량 변경*/
    void updateCartItemQuantity(Long cartId, Integer quantity);

    /*장바구니 아이템 삭제*/
    void deleteCartItem(Long cartItemId);
}
