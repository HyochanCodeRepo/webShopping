package com.example.webshopping.repository;

import com.example.webshopping.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // 대분류 조회 (parent가 null인 것들)
    List<Category> findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
    
    // 특정 부모의 하위 카테고리 조회
    List<Category> findByParent_IdAndIsActiveTrueOrderByDisplayOrderAsc(Long parentId);
    
    // depth별 카테고리 조회
    List<Category> findByDepthAndIsActiveTrueOrderByDisplayOrderAsc(Integer depth);
    
    // 코드로 카테고리 조회
    Category findByCode(String code);
}
