package com.example.webshopping.repository;

import com.example.webshopping.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    
    // 특정 상품의 모든 옵션 조회
    List<ProductOption> findByProduct_IdOrderByDisplayOrderAsc(Long productId);
    
    // 특정 상품의 활성화된 옵션만 조회
    List<ProductOption> findByProduct_IdAndIsActiveTrueOrderByDisplayOrderAsc(Long productId);
    
    // 특정 상품의 특정 타입 옵션 조회
    List<ProductOption> findByProduct_IdAndOptionType(Long productId, String optionType);
}
