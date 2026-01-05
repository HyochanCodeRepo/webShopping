package com.example.webshopping.controller;

import com.example.webshopping.constant.OrderStatus;
import com.example.webshopping.constant.Role;
import com.example.webshopping.dto.OrderResponseDTO;
import com.example.webshopping.dto.SellerDTO;
import com.example.webshopping.entity.Members;
import com.example.webshopping.entity.Product;
import com.example.webshopping.repository.MembersRepository;
import com.example.webshopping.service.OrderService;
import com.example.webshopping.service.ProductService;
import com.example.webshopping.service.SellerService;
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
    private final SellerService sellerService;
    private final MembersRepository membersRepository;
    
    /**
     * 관리자 메인 페이지
     */
    @GetMapping
    public String adminPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        
        if (userDetails == null) {
            return "redirect:/members/login";
        }
        
        String email = userDetails.getUsername();
        Members member = membersRepository.findByEmail(email);
        
        log.info("관리자 페이지 접속 - Role: {}", member.getRole());
        
        // SELLER인 경우 판매자 전용 페이지로
        if (member.getRole() == Role.ROLE_SELLER) {
            return "admin/seller-admin";
        }
        
        // ADMIN인 경우 관리자 페이지로
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
        log.info("======== 상품 목록 페이지 접속 ========");
        log.info("로그인 이메일: {}", email);
        
        // 회원 정보 조회
        Members member = membersRepository.findByEmail(email);
        
        List<Product> products;
        
        // ADMIN: 모든 상품 조회
        if (member.getRole() == Role.ROLE_ADMIN) {
            products = productService.getAllProducts();
            log.info("관리자 - 전체 상품 조회: {}건", products.size());
            model.addAttribute("isAdmin", true);
        } 
        // SELLER: 본인 상품만 조회
        else {
            products = productService.getProductsByEmail(email);
            log.info("판매자 - 본인 상품 조회: {}건", products.size());
            model.addAttribute("isAdmin", false);
        }
        
        model.addAttribute("products", products);
        log.info("======================================");
        
        return "admin/product-list";
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
    
    
    // ========== 판매자 신청 관리 ==========
    
    /**
     * 판매자 신청 관리 페이지
     */
    @GetMapping("/sellers")
    public String sellerApplications(Model model) {
        log.info("======== 판매자 신청 관리 페이지 접속 ========");
        
        // 승인 대기 중인 신청 목록
        List<SellerDTO> pendingApplications = sellerService.getPendingApplications();
        
        // 모든 신청 목록
        List<SellerDTO> allApplications = sellerService.getAllApplications();
        
        log.info("승인 대기: {}건", pendingApplications.size());
        log.info("전체 신청: {}건", allApplications.size());
        
        model.addAttribute("pendingApplications", pendingApplications);
        model.addAttribute("allApplications", allApplications);
        log.info("==========================================");
        
        return "admin/seller-management";
    }
    
    /**
     * 판매자 신청 승인
     */
    @PostMapping("/sellers/{sellerId}/approve")
    public String approveApplication(@PathVariable Long sellerId,
                                     RedirectAttributes redirectAttributes) {
        try {
            sellerService.approveApplication(sellerId);
            
            redirectAttributes.addFlashAttribute("message", "판매자 신청이 승인되었습니다.");
            log.info("판매자 신청 승인 완료 - 신청ID: {}", sellerId);
            
        } catch (Exception e) {
            log.error("판매자 신청 승인 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/sellers";
    }
    
    /**
     * 판매자 신청 거부
     */
    @PostMapping("/sellers/{sellerId}/reject")
    public String rejectApplication(@PathVariable Long sellerId,
                                    @RequestParam(required = false) String reason,
                                    RedirectAttributes redirectAttributes) {
        try {
            String rejectReason = (reason != null && !reason.trim().isEmpty()) 
                ? reason 
                : "관리자에 의해 거부됨";
            
            sellerService.rejectApplication(sellerId, rejectReason);
            
            redirectAttributes.addFlashAttribute("message", "판매자 신청이 거부되었습니다.");
            log.info("판매자 신청 거부 완료 - 신청ID: {}, 사유: {}", sellerId, rejectReason);
            
        } catch (Exception e) {
            log.error("판매자 신청 거부 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/sellers";
    }
}
