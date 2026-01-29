package com.example.webshopping.repository;

import com.example.webshopping.constant.ProductType;
import com.example.webshopping.entity.Category;
import com.example.webshopping.entity.Members;
import com.example.webshopping.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ProductRepository 통합 테스트
 * - 복잡한 검색/필터 쿼리 검증
 */
@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MembersRepository membersRepository;

    private Category category1;
    private Category category2;
    private Members member;

    @BeforeEach
    void setUp() {
        // 카테고리 생성
        category1 = Category.builder()
                .name("아웃도어")
                .depth(1)
                .isActive(true)
                .build();

        category2 = Category.builder()
                .name("캠핑")
                .depth(1)
                .isActive(true)
                .build();

        categoryRepository.saveAll(List.of(category1, category2));

        // 회원 생성
        member = Members.builder()
                .email("seller@test.com")
                .name("판매자")
                .password("1234")
                .build();

        membersRepository.save(member);

        // 상품 생성
        Product product1 = Product.builder()
                .productName("등산화")
                .price(100000)
                .stockQuantity(50)
                .category(category1)
                .members(member)
                .productType(ProductType.SHOES)
                .build();

        Product product2 = Product.builder()
                .productName("배낭")
                .price(150000)
                .stockQuantity(30)
                .category(category1)
                .members(member)
                .productType(ProductType.BAG)
                .build();

        Product product3 = Product.builder()
                .productName("텐트")
                .price(200000)
                .stockQuantity(20)
                .category(category2)
                .members(member)
                .productType(ProductType.ETC)
                .build();

        productRepository.saveAll(List.of(product1, product2, product3));
    }

    @Test
    @DisplayName("카테고리별 상품 조회 - 최신순 정렬")
    void 카테고리별_상품조회() {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        // when
        Page<Product> result = productRepository.findByCategoryWithPriceFilter(
                category1.getId(), null, null, pageable
        );

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting("productName")
                .containsExactlyInAnyOrder("등산화", "배낭");
    }

    @Test
    @DisplayName("가격 필터링 - 최소가 ~ 최대가")
    void 가격필터링() {
        // given
        Integer minPrice = 100000;
        Integer maxPrice = 150000;
        Pageable pageable = PageRequest.of(0, 20);

        // when
        Page<Product> result = productRepository.findByCategoryWithPriceFilter(
                category1.getId(), minPrice, maxPrice, pageable
        );

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(p -> p.getPrice() >= minPrice && p.getPrice() <= maxPrice);
    }

    @Test
    @DisplayName("키워드 검색 - 상품명 LIKE 검색")
    void 키워드검색() {
        // given
        String keyword = "등산";
        Pageable pageable = PageRequest.of(0, 20);

        // when
        Page<Product> result = productRepository.searchWithPriceFilter(
                keyword, null, null, null, pageable
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProductName()).contains("등산");
    }

    @Test
    @DisplayName("가격 낮은순 정렬")
    void 가격_낮은순_정렬() {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        // when
        Page<Product> result = productRepository.findByCategoryOrderByPriceAsc(
                category1.getId(), null, null, pageable
        );

        // then
        List<Product> products = result.getContent();
        assertThat(products).hasSize(2);
        assertThat(products.get(0).getPrice()).isLessThanOrEqualTo(products.get(1).getPrice());
    }

    @Test
    @DisplayName("회원별 상품 조회 - 최신순")
    void 회원별_상품조회() {
        // when
        List<Product> products = productRepository.findByMembers_IdOrderByCreatedDateDesc(member.getId());

        // then
        assertThat(products).hasSize(3);
        assertThat(products)
                .extracting("productName")
                .containsExactlyInAnyOrder("등산화", "배낭", "텐트");
    }

    @Test
    @DisplayName("전체 상품 조회 - 최신순 정렬")
    void 전체_상품조회() {
        // when
        List<Product> products = productRepository.findAllByOrderByCreatedDateDesc();

        // then
        assertThat(products).hasSize(3);
    }

    @Test
    @DisplayName("페이징 처리 확인")
    void 페이징처리() {
        // given
        Pageable pageable = PageRequest.of(0, 2); // 페이지당 2개

        // when
        Page<Product> result = productRepository.findByCategoryWithPriceFilter(
                category1.getId(), null, null, pageable
        );

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("복합 검색 - 키워드 + 카테고리 + 가격대")
    void 복합검색() {
        // given
        String keyword = "등산";
        Long categoryId = category1.getId();
        Integer minPrice = 50000;
        Integer maxPrice = 150000;
        Pageable pageable = PageRequest.of(0, 20);

        // when
        Page<Product> result = productRepository.searchWithPriceFilter(
                keyword, categoryId, minPrice, maxPrice, pageable
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        Product product = result.getContent().get(0);
        assertThat(product.getProductName()).contains(keyword);
        assertThat(product.getCategory().getId()).isEqualTo(categoryId);
        assertThat(product.getPrice()).isBetween(minPrice, maxPrice);
    }
}
