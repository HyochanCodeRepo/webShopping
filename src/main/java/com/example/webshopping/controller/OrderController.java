package com.example.webshopping.controller;

import com.example.webshopping.dto.OrderRequestDTO;
import com.example.webshopping.dto.OrderResponseDTO;
import com.example.webshopping.entity.Members;
import com.example.webshopping.repository.MembersRepository;
import com.example.webshopping.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@Log4j2
public class OrderController {

    private final OrderService orderService;
    private final MembersRepository membersRepository;

    @GetMapping("/checkout")
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        if (userDetails == null) {
            return "redirect:/members/login";
        }
        String email = userDetails.getUsername();

        Members members =
            membersRepository.findByEmail(email);

        if (members != null) {
            model.addAttribute("memberName", members.getName());
            model.addAttribute("memberPhone", members.getPhone());
        }

        return "order/checkout";

    }

    /**
     * 주문 생성
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

            log.info("주문 생성 성공 - 주문번호 : {}", orderId);

            return "redirect:/order/success/" + orderId;
        } catch (Exception e) {
            log.error("주문 생성 실패 : {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
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
}
