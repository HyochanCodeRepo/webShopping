package com.example.webshopping.repository;

import com.example.webshopping.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 카테고리별 상품 조회 (품절 상품 맨 뒤 + 최신순)
     * ORDER BY:
     * 1. stockQuantity > 0 DESC : 재고 있는 상품 먼저 (품절 상품 뒤로)
     * 2. createdDate DESC : 최신 등록순
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId " +
           "ORDER BY CASE WHEN p.stockQuantity > 0 THEN 0 ELSE 1 END, p.createdDate DESC")
    List<Product> findByCategory_Id(@Param("categoryId") Long categoryId);

    List<Product> findByMembers_IdOrderByCreatedDateDesc(Long memberId);
    
    List<Product> findAllByOrderByCreatedDateDesc();
    
    /**
     * 오늘의 딜: 할인 중인 상품 (할인율 높은 순, 재고 있는 것만)
     */
    @Query("SELECT p FROM Product p WHERE p.discountRate > 0 AND p.stockQuantity > 0 " +
           "ORDER BY p.discountRate DESC, p.createdDate DESC")
    List<Product> findTodayDeals();
    
    /**
     * 인기 상품: 최신 등록순 (재고 있는 것만)
     * 추후 리뷰/별점 시스템 추가되면 별점순으로 변경 예정
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 " +
           "ORDER BY p.createdDate DESC")
    List<Product> findPopularProducts();
    
    /**
     * 신상품: 최신 등록순 (재고 있는 것만)
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 " +
           "ORDER BY p.createdDate DESC")
    List<Product> findNewProducts();

}

