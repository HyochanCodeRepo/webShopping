package com.example.webshopping.controller;

import com.example.webshopping.dto.SellerDTO;
import com.example.webshopping.entity.Category;
import com.example.webshopping.entity.Members;
import com.example.webshopping.repository.CategoryRepository;
import com.example.webshopping.repository.MembersRepository;
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
@RequestMapping("/seller")
@RequiredArgsConstructor
@Slf4j
public class SellerController {

    private final SellerService sellerService;
    private final MembersRepository membersRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 판매자 신청 페이지
     */
    @GetMapping("/apply")
    public String applyPage(@AuthenticationPrincipal UserDetails userDetails,
                            Model model) {

        if (userDetails == null) {
            return "redirect:/members/login";
        }

        String email = userDetails.getUsername();

        // 이미 승인 대기 중인 신청이 있는지 확인
        if (sellerService.hasPendingApplication(email)) {
            return "redirect:/seller/status";
        }
        
        // 회원 정보 조회해서 Model에 담기
        Members member = membersRepository.findByEmail(email);
        if (member != null) {
            model.addAttribute("memberName", member.getName());
            model.addAttribute("memberPhone", member.getPhone());
            model.addAttribute("memberEmail", email);
        }
        
        // 카테고리 목록 조회
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        return "seller/apply";
    }

    /**
     * 판매자 신청 제출
     */
    @PostMapping("/apply")
    public String submitApplication(@ModelAttribute SellerDTO dto,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/members/login";
        }

        try {
            String email = userDetails.getUsername();
            Long sellerId = sellerService.submitApplication(email, dto);

            log.info("판매자 신청 완료 - 신청ID: {}", sellerId);

            redirectAttributes.addFlashAttribute("message",
                    "판매자 신청이 완료되었습니다. 관리자 승인까지 1-2일 소요됩니다.");

            return "redirect:/seller/status";

        } catch (IllegalStateException e) {
            log.error("판매자 신청 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/apply";

        } catch (Exception e) {
            log.error("판매자 신청 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error",
                    "신청 처리 중 오류가 발생했습니다.");
            return "redirect:/seller/apply";
        }
    }

    /**
     * 내 신청 상태 페이지
     */
    @GetMapping("/status")
    public String applicationStatus(@AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {

        if (userDetails == null) {
            return "redirect:/members/login";
        }

        String email = userDetails.getUsername();
        List<SellerDTO> sellers = sellerService.getMyApplications(email);

        model.addAttribute("sellers", sellers);

        return "seller/status";
    }
    
    /**
     * 판매자 신청 수정 페이지
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        
        if (userDetails == null) {
            return "redirect:/members/login";
        }
        
        try {
            String email = userDetails.getUsername();
            
            // 신청 내역 조회
            SellerDTO seller = sellerService.getApplication(id);
            
            // 본인의 신청인지 확인
            if (!seller.getMemberEmail().equals(email)) {
                redirectAttributes.addFlashAttribute("error", "본인의 신청만 수정할 수 있습니다.");
                return "redirect:/seller/status";
            }
            
            // 승인 대기 중인 신청만 수정 가능
            if (seller.getStatus().name() != "PENDING") {
                redirectAttributes.addFlashAttribute("error", "승인 대기 중인 신청만 수정할 수 있습니다.");
                return "redirect:/seller/status";
            }
            
            model.addAttribute("seller", seller);
            
            // 카테고리 목록
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);
            
            return "seller/edit";
            
        } catch (Exception e) {
            log.error("판매자 신청 수정 페이지 오류", e);
            redirectAttributes.addFlashAttribute("error", "오류가 발생했습니다.");
            return "redirect:/seller/status";
        }
    }
    
    /**
     * 판매자 신청 수정 처리
     */
    @PostMapping("/edit/{id}")
    public String updateApplication(@PathVariable Long id,
                                    @ModelAttribute SellerDTO dto,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        
        if (userDetails == null) {
            return "redirect:/members/login";
        }
        
        try {
            String email = userDetails.getUsername();
            
            // 신청 내역 조회
            SellerDTO seller = sellerService.getApplication(id);
            
            // 본인의 신청인지 확인
            if (!seller.getMemberEmail().equals(email)) {
                redirectAttributes.addFlashAttribute("error", "본인의 신청만 수정할 수 있습니다.");
                return "redirect:/seller/status";
            }
            
            // 수정 처리
            sellerService.updateApplication(id, dto);
            
            redirectAttributes.addFlashAttribute("message", "신청 내용이 수정되었습니다.");
            return "redirect:/seller/status";
            
        } catch (Exception e) {
            log.error("판매자 신청 수정 실패", e);
            redirectAttributes.addFlashAttribute("error", "수정 중 오류가 발생했습니다.");
            return "redirect:/seller/edit/" + id;
        }
    }
}