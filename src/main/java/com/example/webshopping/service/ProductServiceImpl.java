package com.example.webshopping.service;

import com.example.webshopping.dto.CategoryDTO;
import com.example.webshopping.dto.ProductDTO;
import com.example.webshopping.dto.ProductOptionDTO;
import com.example.webshopping.entity.*;
import com.example.webshopping.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {


    private final ProductRepository productRepository;
    private final ModelMapper modelMapper = new ModelMapper();
    private final CategoryRepository categoryRepository;
    private final FileService fileService;
    private final MembersRepository membersRepository;
    private final ProductDetailRepository productDetailRepository;
    private final CartItemRepository cartItemRepository;




    @Override
    public void create(ProductDTO productDTO, List<String> imageUrls, String email) {

        // ✅ 회원 조회
        Members member = membersRepository.findByEmail(email);
        if (member == null) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다.");
        }

        Product product = Product.builder()
                .productName(productDTO.getProductName())
                .price(productDTO.getPrice())
                .stockQuantity(productDTO.getStockQuantity())
                .description(productDTO.getDescription())
                .discountRate(productDTO.getDiscountRate())
                .productType(productDTO.getProductType())  // ✅ 상품 타입 추가
                .members(member)
                .build();

        //카테고리 설정
        Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow(EntityNotFoundException::new);
        product.setCategory(category);

        //이미지 추가
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < imageUrls.size(); i++) {
                ProductImage image = ProductImage.builder()
                        .imageUrl(imageUrls.get(i))
                        .repImgYn(i == 0 ? "Y" : "N")
                        .imageOrder(i)
                        .build();
                product.addImage(image);
            }
        }

        // ✅ 상품 옵션 추가 (사이즈, 색상 등)
        if (productDTO.getOptions() != null && !productDTO.getOptions().isEmpty()) {
            for (ProductOptionDTO optionDTO : productDTO.getOptions()) {
                ProductOption option = ProductOption.builder()
                        .optionType(optionDTO.getOptionType())
                        .optionValue(optionDTO.getOptionValue())
                        .stockQuantity(optionDTO.getStockQuantity())
                        .additionalPrice(optionDTO.getAdditionalPrice() != null ? optionDTO.getAdditionalPrice() : 0)
                        .displayOrder(optionDTO.getDisplayOrder() != null ? optionDTO.getDisplayOrder() : 0)
                        .isActive(true)
                        .build();
                product.addOption(option);
            }
        }

        // ✅ 상품 저장
        Product savedProduct = productRepository.save(product);
        
        // ✅ 상세 정보 저장 (detailHtml이 있을 경우에만)
        if (productDTO.getDetailHtml() != null && !productDTO.getDetailHtml().trim().isEmpty()) {
            ProductDetail productDetail = ProductDetail.builder()
                    .product(savedProduct)
                    .detailHtml(productDTO.getDetailHtml())
                    .build();
            
            productDetailRepository.save(productDetail);
            
            log.info("상품 상세 정보 저장 완료 - Product ID: {}", savedProduct.getId());
        }
        
        log.info("상품 등록 완료 - ID: {}, 타입: {}, 옵션 수: {}", 
                savedProduct.getId(), 
                savedProduct.getProductType(),
                savedProduct.getOptions().size());
    }

    @Override
    public void update(Long id, ProductDTO productDTO, MultipartFile mainImageFile, List<MultipartFile> detailImageFiles, Boolean keepMainImage, Map<String, String> allParams) {

        //기본 상품조회
        Product product =
            productRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        //기본 정보 업데이트
        product.setProductName(productDTO.getProductName());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setDescription(productDTO.getDescription());
        product.setDiscountRate(productDTO.getDiscountRate());

        //카테고리 업데이트
        Category category =
            categoryRepository.findById(productDTO.getCategoryId()).orElseThrow(EntityNotFoundException::new);
        product.setCategory(category);

        //이미지 처리
        handleImageUpdate(product, mainImageFile, detailImageFiles, keepMainImage, allParams);
        //저장
        productRepository.save(product);

    }

    private void handleImageUpdate(Product product, MultipartFile mainImageFile, List<MultipartFile> detailImageFiles,
                                   Boolean keepMainImage, Map<String, String> allParams) {

        List<ProductImage> existingImages = product.getImages();

        if (!keepMainImage) {
            existingImages.stream()
                    .filter(img -> "Y".equals(img.getRepImgYn()))
                    .forEach(img -> img.setRepImgYn("DELETE"));
        }

        //새 대표이미지 업로드
        if(mainImageFile != null && !mainImageFile.isEmpty()) {
            String mainUrl = fileService.uploadFile(mainImageFile);
            ProductImage mainImage = ProductImage.builder()
                    .imageUrl(mainUrl)
                    .repImgYn("Y")
                    .imageOrder(0)
                    .build();
            product.addImage(mainImage);
        }

        //상세 이미지 처리
        for (int i = 0; i < 4; i++) {
            String keepParam = allParams.get("keepDetailImage" + i);
            if ("false".equals(keepParam)) {
                int finalI = i;
                existingImages.stream()
                        .filter(img -> "N".equals(img.getRepImgYn()))
                        .filter(img -> img.getImageOrder() == finalI + 1)
                        .forEach(img -> img.setRepImgYn("DELETE"));

            }

        }
        //삭제 표시된 이미지들 실제 삭제
        product.getImages().removeIf(img -> "DELETE".equals(img.getRepImgYn()));

        //새 상세 이미지 추가
        if(detailImageFiles != null && !detailImageFiles.isEmpty()) {
            int currentMaxOrder = product.getImages().stream()
                    .mapToInt(ProductImage::getImageOrder)
                    .max()
                    .orElse(0);
            for (int i = 0; i < detailImageFiles.size(); i++) {
                MultipartFile file = detailImageFiles.get(i);
                if (!file.isEmpty()) {
                    String url = fileService.uploadFile(file);
                    ProductImage detailImage = ProductImage.builder()
                            .imageUrl(url)
                            .repImgYn("N")
                            .imageOrder(currentMaxOrder + i + 1)
                            .build();
                    product.addImage(detailImage);
                }
            }
        }

    }

    @Override
    public List<Product> getProductsByEmail(String email) {
        // Members 조회
        Members member = membersRepository.findByEmail(email);

        // 해당 회원이 등록한 상품 목록 조회 (최신순)
        return productRepository.findByMembers_IdOrderByCreatedDateDesc(member.getId());
    }
    
    @Override
    public List<Product> getAllProducts() {
        // 모든 상품 조회 (최신순)
        return productRepository.findAllByOrderByCreatedDateDesc();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 상품 존재 확인
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        // 1. 장바구니 아이템에서 해당 상품 참조 제거 (외래키 제약조건 해결)
        log.info("장바구니에서 상품 제거 중 - Product ID: {}", id);
        cartItemRepository.deleteByProduct_Id(id);
        
        // 2. 주문 아이템은 이력이므로 유지 (삭제하지 않음)
        // (OrderItem -> Product 참조는 남겨둠)
        
        // 3. 리뷰는 Cascade로 자동 삭제 (Product Entity에 설정됨)
        
        // 4. 상품 이미지는 Cascade로 자동 삭제
        
        // 5. 상품 상세는 Cascade로 자동 삭제
        
        // 상품 삭제
        productRepository.delete(product);
        
        log.info("상품 삭제 완료 - Product ID: {}, 상품명: {}", id, product.getProductName());
    }
}
