package com.example.webshopping.repository;

import com.example.webshopping.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCart_IdAndProduct_Id(Long cart_Id, Long product_Id);
    
    // 옵션을 포함한 장바구니 아이템 조회 (옵션 ID가 있는 경우)
    Optional<CartItem> findByCart_IdAndProduct_IdAndProductOption_Id(Long cartId, Long productId, Long productOptionId);
    
    // 옵션이 없는 장바구니 아이템 조회
    Optional<CartItem> findByCart_IdAndProduct_IdAndProductOptionIsNull(Long cartId, Long productId);
    
    // 특정 상품을 포함한 모든 장바구니 아이템 삭제
    void deleteByProduct_Id(Long productId);
}
