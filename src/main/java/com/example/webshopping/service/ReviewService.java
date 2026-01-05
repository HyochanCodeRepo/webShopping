package com.example.webshopping.service;

import com.example.webshopping.dto.ReviewDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {

    /**
     * 리뷰 작성
     */
    void createReview(ReviewDTO reviewDTO, MultipartFile imageFile, String email);

    /**
     * 리뷰 수정
     */
    void updateReview(Long reviewId, ReviewDTO reviewDTO, MultipartFile imageFile, String email);

    /**
     * 리뷰 삭제
     */
    void deleteReview(Long reviewId, String email);

    /**
     * 리뷰 단건 조회
     */
    ReviewDTO getReview(Long reviewId);

    /**
     * 상품별 리뷰 목록 조회
     */
    List<ReviewDTO> getReviewsByProductId(Long productId);

    /**
     * 내가 작성한 리뷰 목록
     */
    List<ReviewDTO> getMyReviews(String email);

    /**
     * 상품 평균 별점
     */
    Double getAverageRating(Long productId);

    /**
     * 상품 리뷰 개수
     */
    Long getReviewCount(Long productId);

    /**
     * 리뷰 작성 가능 여부 (이미 작성했는지)
     */
    boolean canWriteReview(Long productId, String email);
}
