package com.example.webshopping.controller;

import com.example.webshopping.dto.ReviewDTO;
import com.example.webshopping.entity.Product;
import com.example.webshopping.repository.ProductRepository;
import com.example.webshopping.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
@Log4j2
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductRepository productRepository;

    /**
     * 리뷰 작성 페이지
     */
    @GetMapping("/write/{productId}")
    public String writeReviewPage(@PathVariable Long productId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }

        String email = userDetails.getUsername();

        // 리뷰 작성 가능 여부 확인
        if (!reviewService.canWriteReview(productId, email)) {
            return "redirect:/product/detail/" + productId + "?error=already_reviewed";
        }

        // 상품 정보 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        model.addAttribute("product", product);
        return "review/write";
    }

    /**
     * 리뷰 작성 처리
     */
    @PostMapping("/write")
    public String writeReview(@ModelAttribute ReviewDTO reviewDTO,
                              @RequestParam(required = false) MultipartFile imageFile,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }

        try {
            String email = userDetails.getUsername();
            reviewService.createReview(reviewDTO, imageFile, email);

            redirectAttributes.addFlashAttribute("message", "리뷰가 등록되었습니다.");
            return "redirect:/product/detail/" + reviewDTO.getProductId();
        } catch (Exception e) {
            log.error("리뷰 작성 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/product/detail/" + reviewDTO.getProductId();
        }
    }

    /**
     * 리뷰 수정 페이지
     */
    @GetMapping("/edit/{reviewId}")
    public String editReviewPage(@PathVariable Long reviewId,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }

        ReviewDTO review = reviewService.getReview(reviewId);
        Product product = productRepository.findById(review.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        model.addAttribute("review", review);
        model.addAttribute("product", product);
        return "review/edit";
    }

    /**
     * 리뷰 수정 처리
     */
    @PostMapping("/edit/{reviewId}")
    public String editReview(@PathVariable Long reviewId,
                             @ModelAttribute ReviewDTO reviewDTO,
                             @RequestParam(required = false) MultipartFile imageFile,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }

        try {
            String email = userDetails.getUsername();
            reviewService.updateReview(reviewId, reviewDTO, imageFile, email);

            redirectAttributes.addFlashAttribute("message", "리뷰가 수정되었습니다.");
            return "redirect:/product/detail/" + reviewDTO.getProductId();
        } catch (Exception e) {
            log.error("리뷰 수정 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/product/detail/" + reviewDTO.getProductId();
        }
    }

    /**
     * 리뷰 삭제
     */
    @PostMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId,
                               @RequestParam Long productId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }

        try {
            String email = userDetails.getUsername();
            reviewService.deleteReview(reviewId, email);

            redirectAttributes.addFlashAttribute("message", "리뷰가 삭제되었습니다.");
        } catch (Exception e) {
            log.error("리뷰 삭제 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/product/detail/" + productId;
    }

    /**
     * 내 리뷰 목록
     */
    @GetMapping("/my")
    public String myReviews(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }

        String email = userDetails.getUsername();
        model.addAttribute("reviews", reviewService.getMyReviews(email));

        return "review/my-reviews";
    }
}
