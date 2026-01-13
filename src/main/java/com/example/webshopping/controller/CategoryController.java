package com.example.webshopping.controller;

import com.example.webshopping.dto.CategoryDTO;
import com.example.webshopping.service.CategoryService;
import com.example.webshopping.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {
    
    private final CategoryService categoryService;
    private final FileService fileService;
    
    /**
     * 카테고리 관리 페이지 (관리자)
     */
    @GetMapping("/manage")
    public String managePage(Model model) {
        List<CategoryDTO> categories = categoryService.getAllCategoriesWithHierarchy();
        model.addAttribute("categories", categories);
        return "category/manage";
    }
    
    /**
     * 카테고리 등록 페이지
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        List<CategoryDTO> largeCategories = categoryService.getLargeCategories();
        model.addAttribute("largeCategories", largeCategories);
        return "category/form";
    }
    
    /**
     * 카테고리 등록 처리
     */
    @PostMapping("/register")
    public String register(@ModelAttribute CategoryDTO categoryDTO, 
                          @RequestParam(required = false) MultipartFile imageFile,
                          RedirectAttributes redirectAttributes) {
        try {
            String imageUrl = null;
            
            // 이미지 파일이 업로드된 경우
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = fileService.uploadCategoryImage(imageFile);
            }
            
            categoryService.createCategory(categoryDTO, imageUrl);
            redirectAttributes.addFlashAttribute("message", "카테고리가 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            log.error("카테고리 등록 실패", e);
            redirectAttributes.addFlashAttribute("error", "카테고리 등록에 실패했습니다: " + e.getMessage());
        }
        return "redirect:/category/manage";
    }
    
    /**
     * 카테고리 수정 페이지
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        CategoryDTO category = categoryService.getCategoryById(id);
        List<CategoryDTO> largeCategories = categoryService.getLargeCategories();
        
        model.addAttribute("category", category);
        model.addAttribute("largeCategories", largeCategories);
        return "category/form";
    }
    
    /**
     * 카테고리 수정 처리
     */
    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, 
                        @ModelAttribute CategoryDTO categoryDTO, 
                        @RequestParam(required = false) MultipartFile imageFile,
                        RedirectAttributes redirectAttributes) {
        try {
            String imageUrl = null;
            
            // 이미지 파일이 업로드된 경우
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = fileService.uploadCategoryImage(imageFile);
            }
            
            categoryService.updateCategory(id, categoryDTO, imageUrl);
            redirectAttributes.addFlashAttribute("message", "카테고리가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            log.error("카테고리 수정 실패", e);
            redirectAttributes.addFlashAttribute("error", "카테고리 수정에 실패했습니다: " + e.getMessage());
        }
        return "redirect:/category/manage";
    }
    
    /**
     * 카테고리 삭제
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("message", "카테고리가 성공적으로 삭제되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("카테고리 삭제 실패", e);
            redirectAttributes.addFlashAttribute("error", "카테고리 삭제에 실패했습니다: " + e.getMessage());
        }
        return "redirect:/category/manage";
    }
    
    // ========== REST API ==========
    
    /**
     * 특정 부모의 하위 카테고리 조회 (AJAX용)
     */
    @GetMapping("/api/children/{parentId}")
    @ResponseBody
    public ResponseEntity<List<CategoryDTO>> getChildren(@PathVariable Long parentId) {
        List<CategoryDTO> children = categoryService.getChildCategories(parentId);
        return ResponseEntity.ok(children);
    }
    
    /**
     * 카테고리 활성화/비활성화 토글 (AJAX용)
     */
    @PostMapping("/api/toggle/{id}")
    @ResponseBody
    public ResponseEntity<String> toggleStatus(@PathVariable Long id) {
        try {
            categoryService.toggleCategoryStatus(id);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("카테고리 상태 변경 실패", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
