package com.example.webshopping.controller;

import com.example.webshopping.constant.Role;
import com.example.webshopping.dto.ProductDTO;
import com.example.webshopping.entity.Category;
import com.example.webshopping.entity.Members;
import com.example.webshopping.entity.Product;
import com.example.webshopping.repository.CategoryRepository;
import com.example.webshopping.repository.MembersRepository;
import com.example.webshopping.repository.ProductRepository;
import com.example.webshopping.service.FileService;
import com.example.webshopping.service.ProductService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final FileService fileService;
    private final ProductRepository productRepository;
    private final ReviewService reviewService;
    private final MembersRepository membersRepository;

    @GetMapping("/register")
    public String register(Model model) {
        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("categories", categories);

        return "product/form";
    }

    @PostMapping("/register")
    public String register(ProductDTO productDTO,
                           @RequestParam(required = false) MultipartFile mainImageFile,
                           @RequestParam(required = false)List<MultipartFile> detailImageFiles,
                           @AuthenticationPrincipal UserDetails userDetails,  // ✅ 추가
                           RedirectAttributes redirectAttributes) {
        log.info(productDTO);

        // ✅ 로그인 체크
        if (userDetails == null) {
            return "redirect:/members/login";
        }

        String email = userDetails.getUsername();  // ✅ 이메일 가져오기

        List<String> imageUrls = new ArrayList<>();

        //대표 이미지 저장
        if(mainImageFile != null && !mainImageFile.isEmpty()) {
            String mainUrl = fileService.uploadFile(mainImageFile);
            imageUrls.add(mainUrl);
        }

        //상세 이미지 저장
        if (detailImageFiles != null && !detailImageFiles.isEmpty()) {
            for (MultipartFile file : detailImageFiles) {
                if (!file.isEmpty()) {
                    String url = fileService.uploadFile(file);
                    imageUrls.add(url);
                }

            }
        }
        //서비스 호출
        productService.create(productDTO, imageUrls, email);  // ✅ email 추가

        redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 등록 되었습니다.");

        return "redirect:/product/register";
    }

    @GetMapping("/list")
    public String list(@RequestParam Long categoryId, Model model) {
        log.info("categoryId : {}",categoryId);

        List<Product> productList =
                productRepository.findByCategory_Id(categoryId);

        Category category =
            categoryRepository.findById(categoryId).orElseThrow(EntityNotFoundException::new);

        model.addAttribute("categoryName", category.getName());
        model.addAttribute("products", productList);


        return "product/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, 
                        @AuthenticationPrincipal UserDetails userDetails,
                        Model model) {
        log.info("id : {}",id);
        Product product = productRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        model.addAttribute("product", product);
        
        // 리뷰 데이터
        model.addAttribute("reviews", reviewService.getReviewsByProductId(id));
        model.addAttribute("averageRating", reviewService.getAverageRating(id));
        model.addAttribute("reviewCount", reviewService.getReviewCount(id));
        
        // 리뷰 작성 가능 여부 및 현재 사용자 ID
        if (userDetails != null) {
            String email = userDetails.getUsername();
            Members currentMember = membersRepository.findByEmail(email);
            model.addAttribute("canWriteReview", reviewService.canWriteReview(id, email));
            model.addAttribute("currentMemberId", currentMember != null ? currentMember.getId() : null);
        } else {
            model.addAttribute("canWriteReview", false);
            model.addAttribute("currentMemberId", null);
        }
        
        log.info("상품 상세 조회 완료 - Product ID: {}, ProductDetail: {}", 
                id, product.getProductDetail() != null ? "있음" : "없음");
        
        return "product/detail";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        Product product =
            productRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        Members currentMember = membersRepository.findByEmail(userDetails.getUsername());

        if (currentMember.getRole() != Role.ROLE_ADMIN && !product.getMembers().getId().equals(currentMember.getId())) {

            redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/product/detail/" + id;
        }

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        return "product/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       ProductDTO productDTO,
                       @RequestParam(required = false) MultipartFile mainImageFile,
                       @RequestParam(required = false) List<MultipartFile> detailImageFiles,
                       @RequestParam(required = false, defaultValue = "false") Boolean keepMainImage,
                       @RequestParam(required = false) Map<String, String> allParams,
                       @AuthenticationPrincipal UserDetails userDetails,
                       RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }

        Product product = productRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        Members currentMember = membersRepository.findByEmail(userDetails.getUsername());

        if (currentMember.getRole() != Role.ROLE_ADMIN && !product.getMembers().getId().equals(currentMember.getId())) {
            redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/product/detail/" + id;
        }


        productService.update(id, productDTO, mainImageFile, detailImageFiles, keepMainImage, allParams);

        redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 수정되었습니다.");

        return "redirect:/product/detail/" + id;

    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        if(userDetails == null) {
            return "redirect:/members/login";
        }

        Product product =
            productRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        Members currentMember = membersRepository.findByEmail(userDetails.getUsername());
        Long categoryId = product.getCategory().getId();

        if (currentMember.getRole() != Role.ROLE_ADMIN && !product.getMembers().getId().equals(currentMember.getId())) {
            redirectAttributes.addFlashAttribute("error", "삭제 권한이 없습니다.");
            return "redirect:/product/detail/" + id;
        }


        productRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 삭제되었습니다.");

        return "redirect:/product/list?categoryId=" + categoryId;
    }



}
