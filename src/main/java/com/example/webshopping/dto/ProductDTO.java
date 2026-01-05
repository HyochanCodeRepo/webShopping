package com.example.webshopping.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String productName;
    private Integer price;
    private Integer stockQuantity;
    private String description;
//    private String imageUrl;
    private Integer discountRate;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String repImageUrl;

    //상세 이미지 조회용
    @Builder.Default
    private List<String> detailImageUrls = new ArrayList<>();
    
    // ========== 상세 정보 (CKEditor) ==========
    private String detailHtml;  // CKEditor로 작성한 HTML
    
    @Builder.Default
    private List<String> additionalDetailImages = new ArrayList<>();  // 추가 상세 이미지
}
