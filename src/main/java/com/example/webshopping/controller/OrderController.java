package com.example.webshopping.controller;

import com.example.webshopping.dto.OrderRequestDTO;
import com.example.webshopping.dto.OrderResponseDTO;
import com.example.webshopping.dto.PaymentRequestDTO;
import com.example.webshopping.entity.Members;
import com.example.webshopping.repository.MembersRepository;
import com.example.webshopping.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@Log4j2
public class OrderController {

    private final OrderService orderService;
    private final MembersRepository membersRepository;
    
    @Value("${toss.payments.client-key}")
    private String clientKey;

    @GetMapping("/checkout")
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        if (userDetails == null) {
            return "redirect:/members/login";
        }
        String email = userDetails.getUsername();

        Members members = membersRepository.findByEmail(email);

        if (members != null) {
            model.addAttribute("memberName", members.getName());
            model.addAttribute("memberPhone", members.getPhone());
        }
        
        // 토스페이먼츠 클라이언트 키 전달
        model.addAttribute("tossClientKey", clientKey);

        return "order/checkout";

    }

    /**
     * 일반 주문 생성 (결제 없음)
     */
    @PostMapping("/create")
    public String createOrder(@ModelAttribute OrderRequestDTO orderRequestDTO,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }
        try {
            String email = userDetails.getUsername();
            Long orderId = orderService.createOrder(email, orderRequestDTO);

            log.info("✅ 일반 주문 생성 성공 - 주문번호: {}", orderId);

            return "redirect:/order/success/" + orderId;
        } catch (Exception e) {
            log.error("❌ 주문 생성 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }
    
    /**
     * 결제용 주문 생성 (토스페이먼츠)
     */
    @PostMapping("/create-for-payment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOrderForPayment(
            @RequestBody OrderRequestDTO orderRequestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        
        try {
            String email = userDetails.getUsername();
            Long orderId = orderService.createOrderForPayment(email, orderRequestDTO);
            
            OrderResponseDTO order = orderService.getOrder(orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", order.getOrderIdForPayment());  // UUID
            response.put("amount", order.getTotalPrice());
            response.put("orderName", "주문 " + orderId);  // 주문명
            
            log.info("✅ 결제용 주문 생성 성공 - orderId: {}, amount: {}", 
                     order.getOrderIdForPayment(), order.getTotalPrice());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 결제용 주문 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 결제 승인 처리
     */
    @PostMapping("/confirm-payment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmPayment(@RequestBody PaymentRequestDTO paymentRequestDTO) {
        
        try {
            orderService.confirmOrderPayment(paymentRequestDTO);
            
            log.info("✅ 결제 승인 성공 - orderId: {}", paymentRequestDTO.getOrderId());
            
            return ResponseEntity.ok(Map.of("success", true));
            
        } catch (Exception e) {
            log.error("❌ 결제 승인 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 주문 완료 페이지
     */
    @GetMapping("/success/{orderId}")
    public String orderSuccess(@PathVariable Long orderId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }
        OrderResponseDTO order = orderService.getOrder(orderId);
        model.addAttribute("order", order);
        return "order/success";
    }

    /**
     * 주문 내역 목록
     */
    @GetMapping("/list")
    public String orderList(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }

        String email = userDetails.getUsername();
        List<OrderResponseDTO> orders = orderService.getMyOrders(email);

        model.addAttribute("orders", orders);

        return "order/list";
    }

    /**
     * 주문 상세
     * */
    @GetMapping("/detail/{orderId}")
    public String orderDetail(@PathVariable Long orderId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }
        OrderResponseDTO order = orderService.getOrder(orderId);
        model.addAttribute("order", order);

        return "order/detail";
    }

    /**
     * 주문 취소
     * */
    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(orderId);
            redirectAttributes.addFlashAttribute("message", "주문이 취소되었습니다.");
        }catch (Exception e) {
            log.error("주문 취소 실패 : {}",e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/order/detail/" + orderId;
    }
    
    /**
     * 토스 결제 성공 (콜백)
     */
    @GetMapping("/payment-success")
    public String paymentSuccess(@RequestParam String paymentKey,
                                   @RequestParam String orderId,
                                   @RequestParam Integer amount,
                                   RedirectAttributes redirectAttributes) {
        
        log.info("✅ 토스 결제 성공 콜백 - orderId: {}, amount: {}", orderId, amount);
        
        try {
            // 결제 승인 요청
            PaymentRequestDTO paymentRequest = PaymentRequestDTO.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .build();
            
            orderService.confirmOrderPayment(paymentRequest);
            
            redirectAttributes.addFlashAttribute("message", "결제가 완료되었습니다!");
            
            // 주문 번호로 조회
            return "redirect:/order/list";
            
        } catch (Exception e) {
            log.error("❌ 결제 승인 실패: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "결제 승인 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/cart";
        }
    }
    
    /**
     * 토스 결제 실패 (콜백)
     */
    @GetMapping("/payment-fail")
    public String paymentFail(@RequestParam String code,
                               @RequestParam String message,
                               RedirectAttributes redirectAttributes) {
        
        log.warn("❌ 토스 결제 실패 - code: {}, message: {}", code, message);
        
        redirectAttributes.addFlashAttribute("error", "결제가 취소되었습니다: " + message);
        return "redirect:/cart";
    }
    
    /**
     * 내 주문 업데이트 건수 조회 API (일반 사용자용)
     * 마지막 확인 시간 이후 상태 변경된 주문 건수
     */
    @GetMapping("/api/my-updates-count")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getMyOrderUpdatesCount(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String lastChecked) {
        
        if (userDetails == null) {
            return ResponseEntity.ok(Map.of("count", 0L));
        }
        
        try {
            String email = userDetails.getUsername();
            Members member = membersRepository.findByEmail(email);
            
            if (member == null) {
                return ResponseEntity.ok(Map.of("count", 0L));
            }
            
            // 마지막 확인 시간 파싱 (없으면 24시간 전)
            LocalDateTime lastCheckedTime;
            if (lastChecked != null && !lastChecked.isEmpty()) {
                lastCheckedTime = LocalDateTime.parse(lastChecked);
            } else {
                lastCheckedTime = LocalDateTime.now().minusDays(1);
            }
            
            Long count = orderService.countUpdatedOrders(member.getId(), lastCheckedTime);
            
            return ResponseEntity.ok(Map.of("count", count));
            
        } catch (Exception e) {
            log.error("주문 업데이트 건수 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("count", 0L));
        }
    }
}
