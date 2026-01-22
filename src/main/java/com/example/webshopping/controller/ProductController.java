package com.example.webshopping.controller;

import com.example.webshopping.constant.ProductType;
import com.example.webshopping.constant.Role;
import com.example.webshopping.dto.CategoryDTO;
import com.example.webshopping.dto.ProductDTO;
import com.example.webshopping.dto.ProductOptionDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
import java.util.stream.Collectors;

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
        // 대분류 카테고리만 조회
        List<Category> categories = categoryRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
        
        // ProductType enum 전달
        model.addAttribute("categories", categories);
        model.addAttribute("productTypes", ProductType.values());

        return "product/form";
    }
    
    /**
     * 중분류 카테고리 조회 API
     */
    @GetMapping("/api/categories/{parentId}/children")
    @ResponseBody
    public ResponseEntity<List<CategoryDTO>> getChildCategories(@PathVariable Long parentId) {
        List<Category> children = categoryRepository.findByParent_IdAndIsActiveTrueOrderByDisplayOrderAsc(parentId);
        
        List<CategoryDTO> categoryDTOs = children.stream()
                .map(cat -> CategoryDTO.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .depth(cat.getDepth())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(categoryDTOs);
    }
    
    /**
     * 상품 타입별 기본 사이즈 조회 API
     */
    @GetMapping("/api/product-types/{productType}/sizes")
    @ResponseBody
    public ResponseEntity<String[]> getDefaultSizes(@PathVariable ProductType productType) {



        return ResponseEntity.ok(productType.getDefaultSizeArray());
    }

    @PostMapping("/register")
    public String register(@ModelAttribute ProductDTO productDTO,
                           @RequestParam(required = false) MultipartFile mainImageFile,
                           @RequestParam(required = false)MultipartFile[] detailImageFiles,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        
        // ✅ 디버깅 로그
        log.info("=== 상품 등록 요청 ===");
        log.info("ProductDTO: {}", productDTO);
        log.info("CategoryId: {}", productDTO.getCategoryId());
        log.info("MainImage: {}", mainImageFile != null ? mainImageFile.getOriginalFilename() : "없음");
        log.info("DetailImages: {}", detailImageFiles != null ? detailImageFiles.length : 0);

        // ✅ 로그인 체크
        if (userDetails == null) {
            return "redirect:/members/login";
        }

        // ✅ 카테고리 필수 체크
        if (productDTO.getCategoryId() == null) {
            log.error("❌ 카테고리가 선택되지 않음");
            redirectAttributes.addFlashAttribute("error", "카테고리를 선택해주세요.");
            return "redirect:/product/register";
        }

        // ✅ 대표 이미지 필수 체크
        if (mainImageFile == null || mainImageFile.isEmpty()) {
            log.error("❌ 대표 이미지가 없음");
            redirectAttributes.addFlashAttribute("error", "대표 이미지를 선택해주세요.");
            return "redirect:/product/register";
        }

        String email = userDetails.getUsername();

        List<String> imageUrls = new ArrayList<>();

        //대표 이미지 저장
        if(mainImageFile != null && !mainImageFile.isEmpty()) {
            String mainUrl = fileService.uploadFile(mainImageFile);
            imageUrls.add(mainUrl);
        }

        //상세 이미지 저장
        if (detailImageFiles != null && detailImageFiles.length > 0) {
            for (MultipartFile file : detailImageFiles) {
                if (!file.isEmpty()) {
                    String url = fileService.uploadFile(file);
                    imageUrls.add(url);
                }

            }
        }
        
        try {
            //서비스 호출
            productService.create(productDTO, imageUrls, email);
            log.info("✅ 상품 등록 성공");
            redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 등록 되었습니다.");
        } catch (Exception e) {
            log.error("❌ 상품 등록 실패: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "상품 등록 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/product/register";
    }

    @GetMapping("/list")
    public String list(@RequestParam Long categoryId,
                       @RequestParam(required = false, defaultValue = "latest") String sortBy,
                       @RequestParam(required = false) Integer minPrice,
                       @RequestParam(required = false) Integer maxPrice,
                       @RequestParam(required = false, defaultValue = "0") int page,
                       Model model) {
        log.info("카테고리: {}, 정렬: {}, 가격: {}-{}, 페이지: {}", categoryId, sortBy, minPrice, maxPrice, page);

        // Pageable 생성 (page: 페이지번호, size: 20개씩)
        Pageable pageable = PageRequest.of(page, 20);
        
        Page<Product> productPage;
        
        // 정렬 기준에 따라 쿼리 실행
        switch (sortBy) {
            case "price_asc":
                productPage = productRepository.findByCategoryOrderByPriceAsc(categoryId, minPrice, maxPrice, pageable);
                break;
            case "price_desc":
                productPage = productRepository.findByCategoryOrderByPriceDesc(categoryId, minPrice, maxPrice, pageable);
                break;
            case "popular":
                productPage = productRepository.findByCategoryOrderByPopular(categoryId, minPrice, maxPrice, pageable);
                break;
            case "latest":
            default:
                productPage = productRepository.findByCategoryWithPriceFilter(categoryId, minPrice, maxPrice, pageable);
                break;
        }

        Category category =
            categoryRepository.findById(categoryId).orElseThrow(EntityNotFoundException::new);

        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categoryName", category.getName());
        model.addAttribute("products", productPage.getContent());  // 실제 상품 리스트
        model.addAttribute("productPage", productPage);  // 페이징 정보
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

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
//        Product product =
//            productRepository.findById(id).orElseThrow(EntityNotFoundException::new);
//
//        Members currentMember = membersRepository.findByEmail(userDetails.getUsername());
//
//        if (currentMember.getRole() != Role.ROLE_ADMIN && !product.getMembers().getId().equals(currentMember.getId())) {
//
//            redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
//            return "redirect:/product/detail/" + id;
//        }
//
//        // ✅ Product를 ProductDTO로 변환
//        ProductDTO productDTO = ProductDTO.builder()
//                .id(product.getId())
//                .productName(product.getProductName())
//                .price(product.getPrice())
//                .stockQuantity(product.getStockQuantity())
//                .description(product.getDescription())
//                .discountRate(product.getDiscountRate())
//                .categoryId(product.getCategory().getId())
//                .productType(product.getProductType())
//                .build();
//
//        // ✅ 이미지 URL 리스트 생성
//        if (product.getImages() != null && !product.getImages().isEmpty()) {
//            product.getImages().stream()
//                    .filter(img -> "Y".equals(img.getRepImgYn()))
//                    .findFirst()
//                    .ifPresent(img -> productDTO.setRepImageUrl(img.getImageUrl()));
//
//            List<String> detailUrls = product.getImages().stream()
//                    .filter(img -> "N".equals(img.getRepImgYn()))
//                    .map(img -> img.getImageUrl())
//                    .collect(Collectors.toList());
//            productDTO.setDetailImageUrls(detailUrls);
//        }
//
//        // ✅ 옵션 리스트 변환
//        if (product.getOptions() != null && !product.getOptions().isEmpty()) {
//            List<com.example.webshopping.dto.ProductOptionDTO> optionDTOs = product.getOptions().stream()
//                    .map(option -> com.example.webshopping.dto.ProductOptionDTO.builder()
//                            .id(option.getId())
//                            .optionType(option.getOptionType())
//                            .optionValue(option.getOptionValue())
//                            .stockQuantity(option.getStockQuantity())
//                            .additionalPrice(option.getAdditionalPrice())
//                            .displayOrder(option.getDisplayOrder())
//                            .build())
//                    .collect(Collectors.toList());
//            productDTO.setOptions(optionDTOs);
//        }
//
//        model.addAttribute("product", productDTO);
//        model.addAttribute("categories", categoryRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc());
//        model.addAttribute("productTypes", ProductType.values());
//
//        return "product/form";
        Product product =
                productRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        Members currentMember = membersRepository.findByEmail(userDetails.getUsername());

        if (currentMember.getRole() != Role.ROLE_ADMIN &&
                !product.getMembers().getId().equals(currentMember.getId())) {

            redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/product/detail/" + id;
        }

        ProductDTO productDTO = ProductDTO.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .description(product.getDescription())
                .discountRate(product.getDiscountRate())
                .categoryId(product.getCategory().getId())
                .productType(product.getProductType())
                .build();

        // 대표 / 상세 이미지
        if (product.getImages() != null) {
            product.getImages().stream()
                    .filter(img -> "Y".equals(img.getRepImgYn()))
                    .findFirst()
                    .ifPresent(img -> productDTO.setRepImageUrl(img.getImageUrl()));

            productDTO.setDetailImageUrls(
                    product.getImages().stream()
                            .filter(img -> "N".equals(img.getRepImgYn()))
                            .map(img -> img.getImageUrl())
                            .toList()
            );
        }

        // 옵션
        if (product.getOptions() != null) {
            productDTO.setOptions(
                    product.getOptions().stream()
                            .map(option -> ProductOptionDTO.builder()
                                    .id(option.getId())
                                    .optionType(option.getOptionType())
                                    .optionValue(option.getOptionValue())
                                    .stockQuantity(option.getStockQuantity())
                                    .additionalPrice(option.getAdditionalPrice())
                                    .displayOrder(option.getDisplayOrder())
                                    .build())
                            .toList()
            );
        }

        model.addAttribute("product", productDTO);
        model.addAttribute("categories",
                categoryRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc());
        model.addAttribute("productTypes", ProductType.values());

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


        //TODO
        //main이미지 수정을 안했으면 기존 keepMainImage url을 가져와야되는데 그게 안되서 이미지가 안넘어옴.
        //url을 hidden으로 form에서 보내주면 여기서 받아서 로직처리하는거 해야할듯함.


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

        // ProductService를 통해 삭제
        try {
            productService.delete(id);
            redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            log.error("상품 삭제 실패 - Product ID: {}, Error: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "상품 삭제에 실패했습니다: " + e.getMessage());
        }

        return "redirect:/product/list?categoryId=" + categoryId;
    }
    
    /**
     * 상품 검색
     * @param keyword 검색 키워드
     * @param categoryId 카테고리 ID (선택)
     * @param sortBy 정렬 기준 (latest, price_asc, price_desc, popular)
     * @param minPrice 최소 가격 (선택)
     * @param maxPrice 최대 가격 (선택)
     * @param page 페이지 번호 (0부터 시작)
     */
    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false, defaultValue = "latest") String sortBy,
                         @RequestParam(required = false) Integer minPrice,
                         @RequestParam(required = false) Integer maxPrice,
                         @RequestParam(required = false, defaultValue = "0") int page,
                         Model model) {
        log.info("검색 키워드: {}, 카테고리: {}, 정렬: {}, 가격: {}-{}, 페이지: {}", 
                 keyword, categoryId, sortBy, minPrice, maxPrice, page);
        
        // 빈 문자열 체크
        if (keyword == null || keyword.trim().isEmpty()) {
            return "redirect:/";
        }
        
        // Pageable 생성 (page: 페이지번호, size: 20개씩)
        Pageable pageable = PageRequest.of(page, 20);
        
        Page<Product> productPage;
        String categoryName = "전체";
        
        // 카테고리 정보 조회
        if (categoryId != null && categoryId > 0) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                categoryName = category.getName();
            }
        }
        
        // 정렬 기준에 따라 쿼리 실행
        switch (sortBy) {
            case "price_asc":
                productPage = productRepository.searchOrderByPriceAsc(keyword, categoryId, minPrice, maxPrice, pageable);
                break;
            case "price_desc":
                productPage = productRepository.searchOrderByPriceDesc(keyword, categoryId, minPrice, maxPrice, pageable);
                break;
            case "popular":
                productPage = productRepository.searchOrderByPopular(keyword, categoryId, minPrice, maxPrice, pageable);
                break;
            case "latest":
            default:
                productPage = productRepository.searchWithPriceFilter(keyword, categoryId, minPrice, maxPrice, pageable);
                break;
        }
        
        log.info("검색 결과: {}개 (전체: {}개)", productPage.getContent().size(), productPage.getTotalElements());
        
        // 검색 모드 플래그 추가
        model.addAttribute("isSearchMode", true);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("products", productPage.getContent());  // 실제 상품 리스트
        model.addAttribute("productPage", productPage);  // 페이징 정보
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        
        return "product/list";
    }



}
