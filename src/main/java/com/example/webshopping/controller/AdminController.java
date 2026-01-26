package com.example.webshopping.controller;

import com.example.webshopping.constant.OrderStatus;
import com.example.webshopping.constant.Role;
import com.example.webshopping.dto.OrderResponseDTO;
import com.example.webshopping.dto.SellerDTO;
import com.example.webshopping.entity.Members;
import com.example.webshopping.entity.Order;
import com.example.webshopping.entity.Product;
import com.example.webshopping.repository.MembersRepository;
import com.example.webshopping.repository.OrderRepository;
import com.example.webshopping.service.OrderService;
import com.example.webshopping.service.ProductService;
import com.example.webshopping.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
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
     * 관리자 주문 관리 페이지 (검색/필터/페이징)
     */
    @GetMapping("/orders")
    public String orderManagement(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String dateFilter,  // today, week, month
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model) {
        
        if (userDetails == null) {
            return "redirect:/members/login";
        }
        
        String email = userDetails.getUsername();
        log.info("======== 주문 관리 페이지 접속 ========");
        log.info("로그인 이메일: {}", email);
        log.info("검색어: {}, 상태: {}, 날짜필터: {}, 시작일: {}, 종료일: {}, 정렬: {}, 페이지: {}", 
                 keyword, status, dateFilter, startDate, endDate, sortBy, page);
        
        // 날짜 빠른 필터 처리
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (dateFilter != null) {
            LocalDate today = LocalDate.now();
            switch (dateFilter) {
                case "today":
                    startDate = today;
                    endDate = today;
                    break;
                case "week":
                    startDate = today.minusDays(7);
                    endDate = today;
                    break;
                case "month":
                    startDate = today.minusDays(30);
                    endDate = today;
                    break;
            }
        }
        
        // LocalDate → LocalDateTime 변환
        if (startDate != null) {
            startDateTime = startDate.atStartOfDay();
        }
        if (endDate != null) {
            endDateTime = endDate.atTime(23, 59, 59);
        }
        
        // Pageable 생성
        Pageable pageable = PageRequest.of(page, 20);
        
        // 정렬 기준에 따라 주문 검색
        Page<Order> orderPage;
        switch (sortBy) {
            case "amount_desc":
                orderPage = orderRepository.searchOrdersByAmountDesc(
                    email, keyword, status, startDateTime, endDateTime, pageable);
                break;
            case "amount_asc":
                orderPage = orderRepository.searchOrdersByAmountAsc(
                    email, keyword, status, startDateTime, endDateTime, pageable);
                break;
            case "latest":
            default:
                orderPage = orderRepository.searchOrdersLatest(
                    email, keyword, status, startDateTime, endDateTime, pageable);
                break;
        }
        
        // 주문 → DTO 변환
        List<OrderResponseDTO> orders = orderPage.getContent().stream()
                .map(orderService::convertToDTO)
                .collect(Collectors.toList());
        
        // 상태별 카운트 조회
        List<Object[]> statusCounts = orderRepository.countOrdersByStatus(email, startDateTime, endDateTime);
        Map<String, Long> countMap = new HashMap<>();
        long totalCount = 0;
        
        for (Object[] row : statusCounts) {
            OrderStatus orderStatus = (OrderStatus) row[0];
            Long count = (Long) row[1];
            countMap.put(orderStatus.name(), count);
            totalCount += count;
        }
        
        log.info("검색 결과: {}개 (전체: {}개)", orders.size(), orderPage.getTotalElements());
        log.info("상태별 카운트: {}", countMap);
        
        model.addAttribute("orders", orders);
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("statusCounts", countMap);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sortBy", sortBy);
        
        log.info("======================================");
        
        return "admin/order-management";
    }
    
    /**
     * 주문 상태 변경
     */
    @PostMapping("/orders/{orderId}/status")
    public String updateOrderStatus(@PathVariable Long orderId,
                                    @RequestParam String status,
                                    @RequestParam(required = false) String redirect,
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
        
        // redirect 파라미터가 있으면 해당 URL로, 없으면 기본 페이지로
        if (redirect != null && !redirect.trim().isEmpty()) {
            return "redirect:" + redirect;
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
    
    
    // ========== 대시보드 통계 API ==========
    
    /**
     * 관리자 대시보드 통계 데이터 (REST API)
     * 실시간 카드용 데이터
     */
    @GetMapping("/api/stats/summary")
    @ResponseBody
    public Map<String, Object> getDashboardStats() {
        
        Map<String, Object> stats = new HashMap<>();
        
        // 오늘 매출
        Integer todaySales = orderRepository.getTodaySales();
        Integer yesterdaySales = orderRepository.getYesterdaySales();
        double salesChangeRate = calculateChangeRate(yesterdaySales, todaySales);
        
        // 오늘 주문 건수
        Long todayOrderCount = orderRepository.getTodayOrderCount();
        Long yesterdayOrderCount = orderRepository.getYesterdayOrderCount();
        long orderCountChange = todayOrderCount - yesterdayOrderCount;
        
        // 처리 대기 주문
        Long pendingCount = orderRepository.getPendingOrderCount();
        
        // 신규 회원
        Long todayNewMembers = membersRepository.getTodayNewMemberCount();
        Long yesterdayNewMembers = membersRepository.getYesterdayNewMemberCount();
        long memberCountChange = todayNewMembers - yesterdayNewMembers;
        
        // 전체 회원 수
        Long totalMembers = membersRepository.count();
        
        stats.put("todaySales", todaySales);
        stats.put("salesChangeRate", salesChangeRate);
        stats.put("todayOrderCount", todayOrderCount);
        stats.put("orderCountChange", orderCountChange);
        stats.put("pendingCount", pendingCount);
        stats.put("todayNewMembers", todayNewMembers);
        stats.put("memberCountChange", memberCountChange);
        stats.put("totalMembers", totalMembers);
        
        log.info("대시보드 통계 조회 - 오늘 매출: {}, 오늘 주문: {}, 처리대기: {}, 신규회원: {}", 
                 todaySales, todayOrderCount, pendingCount, todayNewMembers);
        
        return stats;
    }
    
    /**
     * 최근 7일 매출 추이 (차트용)
     */
    @GetMapping("/api/stats/sales-trend")
    @ResponseBody
    public Map<String, Object> getSalesTrend() {
        
        List<Object[]> salesData = orderRepository.getLast7DaysSales();
        
        List<String> dates = salesData.stream()
                .map(row -> row[0].toString())
                .collect(Collectors.toList());
        
        List<Integer> sales = salesData.stream()
                .map(row -> ((Number) row[1]).intValue())
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("sales", sales);
        
        log.info("매출 추이 조회 - 데이터 수: {}", salesData.size());
        
        return result;
    }
    
    /**
     * 주문 상태별 통계 (도넛 차트용)
     */
    @GetMapping("/api/stats/order-status")
    @ResponseBody
    public Map<String, Object> getOrderStatusStats() {
        
        List<Object[]> statusData = orderRepository.getOrderStatusCounts();
        
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusData) {
            OrderStatus status = (OrderStatus) row[0];
            Long count = (Long) row[1];
            statusMap.put(status.name(), count);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", statusMap.keySet());
        result.put("counts", statusMap.values());
        
        log.info("주문 상태 통계 조회 - {}", statusMap);
        
        return result;
    }
    
    /**
     * 증감률 계산 헬퍼 메서드
     */
    private double calculateChangeRate(Integer previous, Integer current) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100;
    }
}
