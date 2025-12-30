package com.example.webshopping.controller;

import com.example.webshopping.constant.OrderStatus;
import com.example.webshopping.dto.OrderResponseDTO;
import com.example.webshopping.entity.Product;
import com.example.webshopping.service.OrderService;
import com.example.webshopping.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final ProductService productService;
    private final OrderService orderService;
    
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
    
    
    // ========== 주문 관리 ==========
    
    /**
     * 관리자 주문 관리 페이지
     */
    @GetMapping("/orders")
    public String orderManagement(@AuthenticationPrincipal UserDetails userDetails, 
                                  Model model) {
        
        if (userDetails == null) {
            return "redirect:/members/login";
        }
        
        String email = userDetails.getUsername();
        log.info("======== 주문 관리 페이지 접속 ========");
        log.info("로그인 이메일: {}", email);
        
        // 내 상품에 대한 진행 중인 주문과 완료된 주문 조회
        List<OrderResponseDTO> activeOrders = orderService.getMyActiveOrders(email);
        List<OrderResponseDTO> completedOrders = orderService.getMyCompletedOrders(email);
        
        log.info("진행중 주문: {}건", activeOrders.size());
        log.info("완료 주문: {}건", completedOrders.size());
        
        if (!activeOrders.isEmpty()) {
            log.info("진행중 주문 목록:");
            activeOrders.forEach(order -> 
                log.info("  - 주문번호: {}, 상태: {}", order.getOrderId(), order.getOrderStatus())
            );
        }
        
        model.addAttribute("activeOrders", activeOrders);
        model.addAttribute("completedOrders", completedOrders);
        log.info("======================================");
        
        return "admin/order-management";
    }
    
    /**
     * 주문 상태 변경
     */
    @PostMapping("/orders/{orderId}/status")
    public String updateOrderStatus(@PathVariable Long orderId,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            OrderStatus newStatus = OrderStatus.valueOf(status);
            orderService.updateOrderStatus(orderId, newStatus);
            
            redirectAttributes.addFlashAttribute("message", "주문 상태가 변경되었습니다.");
            log.info("주문 상태 변경 성공 - 주문번호: {}, 새 상태: {}", orderId, newStatus);
            
        } catch (IllegalArgumentException e) {
            log.error("잘못된 주문 상태: {}", status);
            redirectAttributes.addFlashAttribute("error", "잘못된 주문 상태입니다.");
            
        } catch (Exception e) {
            log.error("주문 상태 변경 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/orders";
    }
}
