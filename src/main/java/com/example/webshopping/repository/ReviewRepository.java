package com.example.webshopping.repository;

import com.example.webshopping.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 상품별 리뷰 조회 (최신순)
     */
    List<Review> findByProduct_IdOrderByCreatedAtDesc(Long productId);

    /**
     * 회원별 리뷰 조회 (최신순)
     */
    List<Review> findByMember_IdOrderByCreatedAtDesc(Long memberId);

    /**
     * 상품의 평균 별점 계산
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    /**
     * 상품의 리뷰 개수
     */
    Long countByProduct_Id(Long productId);

    /**
     * 회원이 해당 상품에 리뷰를 작성했는지 확인
     */
    Optional<Review> findByProduct_IdAndMember_Id(Long productId, Long memberId);

    /**
     * 회원이 해당 상품 리뷰를 작성했는지 여부
     */
    boolean existsByProduct_IdAndMember_Id(Long productId, Long memberId);
}
