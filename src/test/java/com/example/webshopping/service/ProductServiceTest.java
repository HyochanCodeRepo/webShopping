package com.example.webshopping.service;

import com.example.webshopping.dto.ProductDTO;
import com.example.webshopping.entity.Category;
import com.example.webshopping.entity.Members;
import com.example.webshopping.entity.Product;
import com.example.webshopping.entity.ProductImage;
import com.example.webshopping.repository.CategoryRepository;
import com.example.webshopping.repository.MembersRepository;
import com.example.webshopping.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * ProductService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MembersRepository membersRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    @DisplayName("상품 등록 성공 - 이미지와 함께")
    void 상품등록_성공() {
        // given
        String email = "test@test.com";
        Members member = Members.builder()
                .id(1L)
                .email(email)
                .name("테스트유저")
                .build();

        Category category = Category.builder()
                .id(1L)
                .name("아웃도어")
                .build();

        ProductDTO productDTO = ProductDTO.builder()
                .productName("등산화")
                .price(100000)
                .stockQuantity(50)
                .description("좋은 등산화")
                .categoryId(1L)
                .build();

        List<String> imageUrls = List.of(
                "/images/main.jpg",
                "/images/detail1.jpg"
        );

        Product savedProduct = Product.builder()
                .id(1L)
                .productName("등산화")
                .price(100000)
                .stockQuantity(50)
                .members(member)
                .category(category)
                .images(new ArrayList<>())
                .build();

        given(membersRepository.findByEmail(email)).willReturn(member);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);

        // when
        productService.create(productDTO, imageUrls, email);

        // then
        then(membersRepository).should().findByEmail(email);
        then(categoryRepository).should().findById(1L);
        then(productRepository).should().save(any(Product.class));
    }

    @Test
    @DisplayName("상품 등록 실패 - 회원을 찾을 수 없음")
    void 상품등록_실패_회원없음() {
        // given
        String email = "notfound@test.com";
        ProductDTO productDTO = ProductDTO.builder()
                .productName("등산화")
                .price(100000)
                .build();

        given(membersRepository.findByEmail(email)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> productService.create(productDTO, null, email))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("회원을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("상품 등록 실패 - 카테고리를 찾을 수 없음")
    void 상품등록_실패_카테고리없음() {
        // given
        String email = "test@test.com";
        Members member = Members.builder()
                .id(1L)
                .email(email)
                .build();

        ProductDTO productDTO = ProductDTO.builder()
                .productName("등산화")
                .categoryId(999L)
                .build();

        given(membersRepository.findByEmail(email)).willReturn(member);
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.create(productDTO, null, email))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("이메일로 상품 목록 조회 성공")
    void 이메일로_상품목록_조회() {
        // given
        String email = "seller@test.com";
        Members member = Members.builder()
                .id(1L)
                .email(email)
                .build();

        List<Product> products = List.of(
                Product.builder().id(1L).productName("등산화").build(),
                Product.builder().id(2L).productName("배낭").build()
        );

        given(membersRepository.findByEmail(email)).willReturn(member);
        given(productRepository.findByMembers_IdOrderByCreatedDateDesc(1L)).willReturn(products);

        // when
        List<Product> result = productService.getProductsByEmail(email);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProductName()).isEqualTo("등산화");
        assertThat(result.get(1).getProductName()).isEqualTo("배낭");
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void 상품삭제_성공() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
                .id(productId)
                .productName("등산화")
                .build();

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        willDoNothing().given(productRepository).delete(product);

        // when
        productService.delete(productId);

        // then
        then(productRepository).should().findById(productId);
        then(productRepository).should().delete(product);
    }

    @Test
    @DisplayName("상품 삭제 실패 - 상품을 찾을 수 없음")
    void 상품삭제_실패() {
        // given
        Long productId = 999L;
        given(productRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.delete(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");
    }
}
