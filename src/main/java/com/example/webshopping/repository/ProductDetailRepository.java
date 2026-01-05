package com.example.webshopping.repository;

import com.example.webshopping.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {
    
    /**
     * Product ID로 상세 정보 조회
     */
    Optional<ProductDetail> findByProduct_Id(Long productId);
}
