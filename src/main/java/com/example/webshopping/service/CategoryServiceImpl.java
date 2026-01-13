package com.example.webshopping.service;

import com.example.webshopping.dto.CategoryDTO;
import com.example.webshopping.entity.Category;
import com.example.webshopping.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategoriesWithHierarchy() {
        // 대분류만 조회
        List<Category> largeCategories = categoryRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
        
        // 재귀적으로 하위 카테고리 포함해서 DTO 변환
        return largeCategories.stream()
                .map(this::convertToHierarchyDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getLargeCategories() {
        List<Category> categories = categoryRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getChildCategories(Long parentId) {
        List<Category> categories = categoryRepository.findByParent_IdAndIsActiveTrueOrderByDisplayOrderAsc(parentId);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));
        return convertToDTO(category);
    }
    
    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO, String imageUrl) {
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .imageUrl(imageUrl != null ? imageUrl : categoryDTO.getImageUrl())
                .depth(categoryDTO.getDepth())
                .code(categoryDTO.getCode())
                .displayOrder(categoryDTO.getDisplayOrder() != null ? categoryDTO.getDisplayOrder() : 0)
                .isActive(true)
                .build();
        
        // 부모 카테고리 설정
        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 카테고리를 찾을 수 없습니다."));
            category.setParent(parent);
            category.setDepth(parent.getDepth() + 1);
        } else {
            category.setDepth(1);
        }
        
        Category saved = categoryRepository.save(category);
        log.info("카테고리 생성 완료 - ID: {}, 이름: {}, Depth: {}", saved.getId(), saved.getName(), saved.getDepth());
        
        return convertToDTO(saved);
    }
    
    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO, String imageUrl) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));
        
        category.setName(categoryDTO.getName());
        
        // 이미지 URL 업데이트: 새 파일이 업로드되었으면 사용, 아니면 기존 유지
        if (imageUrl != null) {
            category.setImageUrl(imageUrl);
        } else if (categoryDTO.getImageUrl() != null) {
            category.setImageUrl(categoryDTO.getImageUrl());
        }
        
        category.setCode(categoryDTO.getCode());
        category.setDisplayOrder(categoryDTO.getDisplayOrder());
        
        // 부모 카테고리 변경 (depth도 자동 조정)
        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 카테고리를 찾을 수 없습니다."));
            category.setParent(parent);
            category.setDepth(parent.getDepth() + 1);
        }
        
        Category updated = categoryRepository.save(category);
        log.info("카테고리 수정 완료 - ID: {}, 이름: {}", updated.getId(), updated.getName());
        
        return convertToDTO(updated);
    }
    
    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));
        
        // 하위 카테고리가 있는지 확인
        List<Category> children = categoryRepository.findByParent_IdAndIsActiveTrueOrderByDisplayOrderAsc(id);
        if (!children.isEmpty()) {
            throw new IllegalStateException("하위 카테고리가 있어 삭제할 수 없습니다. 먼저 하위 카테고리를 삭제해주세요.");
        }
        
        categoryRepository.delete(category);
        log.info("카테고리 삭제 완료 - ID: {}, 이름: {}", id, category.getName());
    }
    
    @Override
    public void toggleCategoryStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));
        
        category.setIsActive(!category.getIsActive());
        categoryRepository.save(category);
        
        log.info("카테고리 상태 변경 완료 - ID: {}, 활성화: {}", id, category.getIsActive());
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Category 엔티티를 CategoryDTO로 변환 (단순)
     */
    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .imageUrl(category.getImageUrl())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .depth(category.getDepth())
                .code(category.getCode())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .build();
    }
    
    /**
     * Category 엔티티를 CategoryDTO로 변환 (계층 구조 포함)
     */
    private CategoryDTO convertToHierarchyDTO(Category category) {
        CategoryDTO dto = convertToDTO(category);
        
        // 하위 카테고리 재귀 조회
        List<Category> children = categoryRepository.findByParent_IdAndIsActiveTrueOrderByDisplayOrderAsc(category.getId());
        if (!children.isEmpty()) {
            dto.setChildren(children.stream()
                    .map(this::convertToHierarchyDTO)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}
