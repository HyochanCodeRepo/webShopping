package com.example.webshopping.controller;

import com.example.webshopping.entity.Product;
import com.example.webshopping.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final ProductService productService;
    
    /**
     * 관리자 메인 페이지
     */
    @GetMapping
    public String adminPage(Model model) {
        log.info("관리자 페이지 접속");
        return "admin/admin";
    }
    
    /**
     * 상품 목록 페이지
     */
    @GetMapping("/product/list")
    public String productList(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        
        if (userDetails == null) {
            return "redirect:/members/login";
        }
        
        String email = userDetails.getUsername();
        
        // 해당 사용자가 등록한 상품 목록 조회
        List<Product> products = productService.getProductsByEmail(email);
        
        model.addAttribute("products", products);
        
        return "admin/product-list";  // ✅ 관리자 전용 페이지
    }
    
    /**
     * 상품 등록 페이지
     */
    @GetMapping("/product/register")
    public String productRegisterPage() {
        return "redirect:/product/register";  // 기존 상품 등록 페이지로 이동
    }
    
    /**
     * 상품 수정 페이지
     */
    @GetMapping("/product/edit/{id}")
    public String productEditPage(@PathVariable Long id) {
        return "redirect:/product/edit/" + id;  // 기존 상품 수정 페이지로 이동
    }
    
    /**
     * 상품 삭제
     */
    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/admin/product/list";
    }
}
