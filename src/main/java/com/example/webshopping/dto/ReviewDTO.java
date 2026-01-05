package com.example.webshopping.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    
    private Long id;
    private Long productId;
    private String productName; // 상품명 (내 리뷰 목록용)
    private String productImageUrl; // 상품 이미지 (내 리뷰 목록용)
    private Long memberId;
    private String memberName; // 작성자 이름
    private Integer rating; // 1-5
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
