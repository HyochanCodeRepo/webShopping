package com.example.webshopping.controller;

import com.example.webshopping.entity.Product;
import com.example.webshopping.repository.CategoryRepository;
import com.example.webshopping.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MainController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;


    @GetMapping("/")
    public String Main(Model model) {
        
        log.info("======== 메인 페이지 접속 ========");
        
        // 카테고리 목록
        model.addAttribute("categories", categoryRepository.findAll());
        
        // 오늘의 딜 (할인 상품, 최대 8개)
        List<Product> todayDeals = productRepository.findTodayDeals();
        model.addAttribute("todayDeals", todayDeals.size() > 8 ? todayDeals.subList(0, 8) : todayDeals);
        log.info("오늘의 딜: {}개", todayDeals.size());
        
        // 인기 상품 (최신순, 최대 12개 - 캐러셀용)
        List<Product> popularProducts = productRepository.findPopularProducts();
        model.addAttribute("popularProducts", popularProducts.size() > 12 ? popularProducts.subList(0, 12) : popularProducts);
        log.info("인기 상품: {}개", popularProducts.size());
        
        // 신상품 (최신순, 최대 12개 - 캐러셀용)
        List<Product> newProducts = productRepository.findNewProducts();
        model.addAttribute("newProducts", newProducts.size() > 12 ? newProducts.subList(0, 12) : newProducts);
        log.info("신상품: {}개", newProducts.size());
        
        log.info("====================================");

        return "main";
    }


}
