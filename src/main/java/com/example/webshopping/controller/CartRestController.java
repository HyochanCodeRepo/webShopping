package com.example.webshopping.controller;

import com.example.webshopping.dto.CartDTO;
import com.example.webshopping.dto.CartRequestDTO;
import com.example.webshopping.dto.CartResponseDTO;
import com.example.webshopping.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartRestController {

    private final CartService cartService;

    /**
     * 장바구니 상품 추가
     */
    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addCart(
            @RequestBody CartRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            String email = userDetails.getUsername();
            log.info("장바구니 추가 요청 - 사용자: {}, 상품ID: {}, 수량: {}",
                    email, requestDTO.getProductId(), requestDTO.getQuantity());

            cartService.addCart(email, requestDTO.getProductId(), requestDTO.getQuantity());

            CartResponseDTO response = CartResponseDTO.builder()
                    .success(true)
                    .message("장바구니에 추가되었습니다.")
                    .build();

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            log.error("장바구니 추가 실패 - EntityNotFoundException: {}", e.getMessage());

            CartResponseDTO response = CartResponseDTO.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (IllegalStateException e) {
            log.error("장바구니 추가 실패 - IllegalStateException: {}", e.getMessage());

            CartResponseDTO response = CartResponseDTO.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("장바구니 추가 실패 - Exception: {}", e.getMessage());

            CartResponseDTO response = CartResponseDTO.builder()
                    .success(false)
                    .message("장바구니 추가 중 오류가 발생했습니다.")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 장바구니 개수 조회
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            if (userDetails == null) {
                return ResponseEntity.ok(Map.of("count", 0));
            }

            String email = userDetails.getUsername();
            int count = cartService.getCartItemCount(email);

            return ResponseEntity.ok(Map.of("count", count));

        } catch (Exception e) {
            log.error("장바구니 개수 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("count", 0));
        }
    }

    /**
     * 장바구니 아이템 목록 조회
     */
    @GetMapping("/items")
    public ResponseEntity<CartDTO> getCartItems(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String email = userDetails.getUsername();
            CartDTO cartDTO = cartService.getCartDTO(email);

            return ResponseEntity.ok(cartDTO);

        } catch (Exception e) {
            log.error("장바구니 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 장바구니 아이템 수량 변경
     */
    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<CartResponseDTO> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> request) {

        try {
            Integer quantity = request.get("quantity");

            if (quantity == null || quantity < 1) {
                return ResponseEntity.badRequest()
                        .body(CartResponseDTO.builder()
                                .success(false)
                                .message("수량은 1개 이상이어야 합니다.")
                                .build());
            }

            cartService.updateCartItemQuantity(cartItemId, quantity);

            return ResponseEntity.ok(CartResponseDTO.builder()
                    .success(true)
                    .message("수량이 변경되었습니다.")
                    .build());

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(CartResponseDTO.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CartResponseDTO.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("수량 변경 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CartResponseDTO.builder()
                            .success(false)
                            .message("수량 변경 중 오류가 발생했습니다.")
                            .build());
        }
    }

    /**
     * 장바구니 아이템 삭제
     */
    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<CartResponseDTO> deleteCartItem(@PathVariable Long cartItemId) {

        try {
            cartService.deleteCartItem(cartItemId);

            return ResponseEntity.ok(CartResponseDTO.builder()
                    .success(true)
                    .message("상품이 삭제되었습니다.")
                    .build());

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(CartResponseDTO.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CartResponseDTO.builder()
                            .success(false)
                            .message("삭제 중 오류가 발생했습니다.")
                            .build());
        }
    }
}