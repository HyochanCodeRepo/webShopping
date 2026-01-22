package com.example.webshopping.repository;

import com.example.webshopping.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCart_IdAndProduct_Id(Long cart_Id, Long product_Id);
    
    // 특정 상품을 포함한 모든 장바구니 아이템 삭제
    void deleteByProduct_Id(Long productId);
}
