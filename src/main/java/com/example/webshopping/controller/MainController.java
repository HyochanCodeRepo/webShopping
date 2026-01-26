package com.example.webshopping.controller;

import com.example.webshopping.entity.Product;
import com.example.webshopping.repository.CategoryRepository;
import com.example.webshopping.repository.OrderRepository;
import com.example.webshopping.repository.ProductRepository;
import com.example.webshopping.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MainController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;


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
        List<Product> popularProductsLimited = popularProducts.size() > 12 ? popularProducts.subList(0, 12) : popularProducts;
        model.addAttribute("popularProducts", popularProductsLimited);
        log.info("인기 상품: {}개", popularProducts.size());
        
        // 인기 상품 별점 데이터 추가
        Map<Long, Double> productRatings = new HashMap<>();
        Map<Long, Long> productReviewCounts = new HashMap<>();
        for (Product product : popularProductsLimited) {
            Double avgRating = reviewRepository.getAverageRatingByProductId(product.getId());
            Long reviewCount = reviewRepository.countByProduct_Id(product.getId());
            productRatings.put(product.getId(), avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);
            productReviewCounts.put(product.getId(), reviewCount);
        }
        model.addAttribute("productRatings", productRatings);
        model.addAttribute("productReviewCounts", productReviewCounts);
        
        // 신상품 (최신순, 최대 12개 - 캐러셀용)
        List<Product> newProducts = productRepository.findNewProducts();
        List<Product> newProductsLimited = newProducts.size() > 12 ? newProducts.subList(0, 12) : newProducts;
        model.addAttribute("newProducts", newProductsLimited);
        log.info("신상품: {}개", newProducts.size());
        
        // 신상품 별점 데이터 추가
        Map<Long, Double> newProductRatings = new HashMap<>();
        Map<Long, Long> newProductReviewCounts = new HashMap<>();
        for (Product product : newProductsLimited) {
            Double avgRating = reviewRepository.getAverageRatingByProductId(product.getId());
            Long reviewCount = reviewRepository.countByProduct_Id(product.getId());
            newProductRatings.put(product.getId(), avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);
            newProductReviewCounts.put(product.getId(), reviewCount);
        }
        model.addAttribute("newProductRatings", newProductRatings);
        model.addAttribute("newProductReviewCounts", newProductReviewCounts);
        
        log.info("====================================");

        return "main";
    }
    
    /**
     * 새 주문 건수 조회 API (Header용)
     * 처리 대기 중인 주문 건수 반환
     */
    @GetMapping("/api/new-orders-count")
    @ResponseBody
    public Map<String, Long> getNewOrdersCount() {
        Long pendingCount = orderRepository.getPendingOrderCount();
        Map<String, Long> response = new HashMap<>();
        response.put("count", pendingCount);
        return response;
    }

}
