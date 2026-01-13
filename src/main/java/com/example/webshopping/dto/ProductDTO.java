package com.example.webshopping.dto;

import com.example.webshopping.constant.ProductType;
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
    
    // ========== 카테고리 세분화 ==========
    private String categoryName;  // 카테고리 이름
    private Long parentCategoryId;  // 부모 카테고리 ID
    
    // ========== 상품 타입 ==========
    private ProductType productType;  // 의류, 신발, 가방 등
    private String productTypeDescription;  // 상품 타입 설명
    
    // ========== 상품 옵션 ==========
    @Builder.Default
    private List<ProductOptionDTO> options = new ArrayList<>();  // 사이즈, 색상 등
}
