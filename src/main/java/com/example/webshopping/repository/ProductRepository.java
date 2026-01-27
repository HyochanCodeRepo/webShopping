package com.example.webshopping.repository;

import com.example.webshopping.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 카테고리별 상품 조회 (품절 상품 맨 뒤 + 최신순)
     * Fetch Join으로 N+1 문제 해결
     * ORDER BY:
     * 1. stockQuantity > 0 DESC : 재고 있는 상품 먼저 (품절 상품 뒤로)
     * 2. createdDate DESC : 최신 등록순
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.category.id = :categoryId " +
           "ORDER BY CASE WHEN p.stockQuantity > 0 THEN 0 ELSE 1 END, p.createdDate DESC")
    List<Product> findByCategory_Id(@Param("categoryId") Long categoryId);
    
    /**
     * 카테고리별 상품 조회 (모든 하위 카테고리 포함 - 재귀)
     * - 대분류 선택 시: 대분류 + 모든 중분류 + 모든 소분류 상품 조회
     * - 중분류 선택 시: 중분류 + 모든 소분류 상품 조회
     * - 소분류 선택 시: 해당 소분류 상품만 조회
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.category.id = :categoryId " +
           "OR p.category.parent.id = :categoryId " +
           "OR p.category.parent.parent.id = :categoryId " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "ORDER BY CASE WHEN p.stockQuantity > 0 THEN 0 ELSE 1 END, p.createdDate DESC")
    Page<Product> findByCategoryWithPriceFilter(
        @Param("categoryId") Long categoryId,
        @Param("minPrice") Integer minPrice,
        @Param("maxPrice") Integer maxPrice,
        Pageable pageable
    );
    
    /**
     * 카테고리 + 가격 낮은순 (모든 하위 카테고리 포함)
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.category.id = :categoryId " +
           "OR p.category.parent.id = :categoryId " +
           "OR p.category.parent.parent.id = :categoryId " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "ORDER BY CASE WHEN p.stockQuantity > 0 THEN 0 ELSE 1 END, p.price ASC")
    Page<Product> findByCategoryOrderByPriceAsc(
        @Param("categoryId") Long categoryId,
        @Param("minPrice") Integer minPrice,
        @Param("maxPrice") Integer maxPrice,
        Pageable pageable
    );
    
    /**
     * 카테고리 + 가격 높은순 (모든 하위 카테고리 포함)
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.category.id = :categoryId " +
           "OR p.category.parent.id = :categoryId " +
           "OR p.category.parent.parent.id = :categoryId " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "ORDER BY CASE WHEN p.stockQuantity > 0 THEN 0 ELSE 1 END, p.price DESC")
    Page<Product> findByCategoryOrderByPriceDesc(
        @Param("categoryId") Long categoryId,
        @Param("minPrice") Integer minPrice,
        @Param("maxPrice") Integer maxPrice,
        Pageable pageable
    );
    
    /**
     * 카테고리 + 인기순 (모든 하위 카테고리 포함)
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN p.reviews r " +
           "WHERE p.category.id = :categoryId " +
           "OR p.category.parent.id = :categoryId " +
           "OR p.category.parent.parent.id = :categoryId " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "GROUP BY p " +
           "ORDER BY CASE WHEN p.stockQuantity > 0 THEN 0 ELSE 1 END, COUNT(r) DESC, p.createdDate DESC")
    Page<Product> findByCategoryOrderByPopular(
        @Param("categoryId") Long categoryId,
        @Param("minPrice") Integer minPrice,
        @Param("maxPrice") Integer maxPrice,
        Pageable pageable
    );

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
    
    /**
     * 상품명으로 검색 (LIKE 검색, 재고 있는 상품 우선)
     * Fetch Join으로 N+1 문제 해결
     * @param keyword 검색 키워드
     * @return 검색 결과 상품 리스트
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.productName LIKE %:keyword% " +
           "ORDER BY CASE WHEN p.stockQuantity > 0 THEN 0 ELSE 1 END, p.createdDate DESC")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 상품명 + 카테고리로 검색
     * Fetch Join으로 N+1 문제 해결
     * @param keyword 검색 키워드
     * @param categoryId 카테고리 ID (null 가능)
     * @return 검색 결과 상품 리스트
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.productName LIKE %:keyword% " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "ORDER BY CASE WHEN p.stockQuantity > 0 THEN 0 ELSE 1 END, p.createdDate DESC")
    List<Product> searchByKeywordAndCategory(
        @Param("keyword") String keyword, 
        @Param("categoryId") Long categoryId
    );
    
    /**
     * 상품명 검색 + 가격 필터링 - 페이징
     * @param keyword 검색 키워드
     * @param categoryId 카테고리 ID (null 가능)
     * @param minPrice 최소 가격 (null 가능)
     * @param maxPrice 최대 가격 (null 가능)
     * @return 검색 결과 상품 리스트
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.productName LIKE %:keyword% " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "ORDER BY CASE WHEN p.stockQuantity > 0 THEN 0 ELSE 1 END, p.createdDate DESC")
    Page<Product> searchWithPriceFilter(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("minPrice") Integer minPrice,
        @Param("maxPrice") Integer maxPrice,
        Pageable pageable
    );
    
    /**
     * 상품명 검색 + 가격 낮은순 - 페이징
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.productName LIKE %:keyword% " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "ORDER BY CASE WHEN p.stockQuantity > 0 THEN 0 ELSE 1 END, p.price ASC")
    Page<Product> searchOrderByPriceAsc(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("minPrice") Integer minPrice,
        @Param("maxPrice") Integer maxPrice,
        Pageable pageable
    );
    
    /**
     * 상품명 검색 + 가격 높은순 - 페이징
     */
    @Query("SELECT p FROM Product p " +
           "WHERE p.productName LIKE %:keyword% " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "ORDER BY CASE WHEN p.stockQuantity > 0 THEN 0 ELSE 1 END, p.price DESC")
    Page<Product> searchOrderByPriceDesc(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("minPrice") Integer minPrice,
        @Param("maxPrice") Integer maxPrice,
        Pageable pageable
    );
    
    /**
     * 상품명 검색 + 인기순 (리뷰 많은순) - 페이징
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN p.reviews r " +
           "WHERE p.productName LIKE %:keyword% " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "GROUP BY p " +
           "ORDER BY CASE WHEN p.stockQuantity > 0 THEN 0 ELSE 1 END, COUNT(r) DESC, p.createdDate DESC")
    Page<Product> searchOrderByPopular(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("minPrice") Integer minPrice,
        @Param("maxPrice") Integer maxPrice,
        Pageable pageable
    );

}
