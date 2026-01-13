package com.example.webshopping.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private Long parentId;        // 부모 카테고리 ID
    private String parentName;    // 부모 카테고리 이름
    private Integer depth;        // 1: 대분류, 2: 중분류, 3: 소분류
    private String code;          // 카테고리 코드
    private Integer displayOrder; // 정렬 순서
    private Boolean isActive;     // 사용 여부
    
    @Builder.Default
    private List<CategoryDTO> children = new ArrayList<>();  // 하위 카테고리 목록
}
