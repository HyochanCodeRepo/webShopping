package com.example.webshopping.config;

import com.example.webshopping.dto.CategoryDTO;
import com.example.webshopping.entity.Category;
import com.example.webshopping.repository.CategoryRepository;
import com.example.webshopping.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    
    /**
     * 모든 페이지에서 카테고리 목록을 사용할 수 있도록 설정
     */
    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        // 계층형 카테고리 (대분류 + 하위 카테고리 포함)
        List<CategoryDTO> hierarchyCategories = categoryService.getAllCategoriesWithHierarchy();
        model.addAttribute("globalCategories", hierarchyCategories);
        
        // 대분류만 (헤더 메뉴용)
        List<CategoryDTO> largeCategories = categoryService.getLargeCategories();
        model.addAttribute("globalLargeCategories", largeCategories);
    }
}
