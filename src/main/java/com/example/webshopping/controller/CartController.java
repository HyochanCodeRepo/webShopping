package com.example.webshopping.controller;

import com.example.webshopping.dto.CartDTO;
import com.example.webshopping.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 페이지 (HTML 반환)
     */
    @GetMapping
    public String cartPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        if (userDetails == null) {
            return "redirect:/members/login";
        }

        String email = userDetails.getUsername();
        CartDTO cartDTO = cartService.getCartDTO(email);

        model.addAttribute("cart", cartDTO);

        return "cart/cart";
    }
}