package com.example.webshopping.service;

import com.example.webshopping.dto.CategoryDTO;
import com.example.webshopping.entity.Category;

import java.util.List;

public interface CategoryService {
    
    /**
     * 모든 카테고리 조회 (계층형 구조)
     */
    List<CategoryDTO> getAllCategoriesWithHierarchy();
    
    /**
     * 대분류 카테고리 조회
     */
    List<CategoryDTO> getLargeCategories();
    
    /**
     * 특정 부모의 하위 카테고리 조회
     */
    List<CategoryDTO> getChildCategories(Long parentId);
    
    /**
     * 카테고리 상세 조회
     */
    CategoryDTO getCategoryById(Long id);
    
    /**
     * 카테고리 생성
     */
    CategoryDTO createCategory(CategoryDTO categoryDTO, String imageUrl);
    
    /**
     * 카테고리 수정
     */
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO, String imageUrl);
    
    /**
     * 카테고리 삭제
     */
    void deleteCategory(Long id);
    
    /**
     * 카테고리 활성화/비활성화
     */
    void toggleCategoryStatus(Long id);
}
