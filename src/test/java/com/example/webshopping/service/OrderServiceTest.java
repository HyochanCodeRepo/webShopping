package com.example.webshopping.service;

import com.example.webshopping.constant.OrderStatus;
import com.example.webshopping.entity.*;
import com.example.webshopping.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;

/**
 * OrderService 단위 테스트
 * - 주문 생성 및 재고 차감 로직 검증
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MembersRepository membersRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("재고 차감 로직 검증 - OrderItem.createOrderItem() 테스트")
    void 재고차감_성공() {
        // given
        Product product = Product.builder()
                .id(1L)
                .productName("등산화")
                .price(100000)
                .stockQuantity(50)
                .build();

        // when
        OrderItem orderItem = OrderItem.createOrderItem(product, null, 5);

        // then
        assertThat(product.getStockQuantity()).isEqualTo(45); // 50 - 5 = 45
        assertThat(orderItem.getQuantity()).isEqualTo(5);
        assertThat(orderItem.getOrderPrice()).isEqualTo(100000);
    }

    @Test
    @DisplayName("재고 차감 - 옵션이 있는 경우")
    void 재고차감_옵션있음() {
        // given
        Product product = Product.builder()
                .id(1L)
                .productName("등산화")
                .price(100000)
                .stockQuantity(50)
                .build();

        ProductOption option = ProductOption.builder()
                .id(1L)
                .optionType("사이즈")
                .optionValue("270")
                .stockQuantity(20)
                .additionalPrice(5000)
                .build();

        // when
        OrderItem orderItem = OrderItem.createOrderItem(product, option, 3);

        // then
        assertThat(option.getStockQuantity()).isEqualTo(17); // 20 - 3 = 17
        assertThat(product.getStockQuantity()).isEqualTo(50); // 상품 재고는 그대로
        assertThat(orderItem.getOrderPrice()).isEqualTo(105000); // 100000 + 5000
    }

    @Test
    @DisplayName("재고 복구 로직 검증 - OrderItem.cancel() 테스트")
    void 재고복구_성공() {
        // given
        Product product = Product.builder()
                .id(1L)
                .productName("등산화")
                .stockQuantity(45) // 차감된 상태
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .product(product)
                .productOption(null)
                .quantity(5)
                .orderPrice(100000)
                .build();

        // when
        orderItem.cancel();

        // then
        assertThat(product.getStockQuantity()).isEqualTo(50); // 45 + 5 = 50 (복구)
    }

    @Test
    @DisplayName("재고 복구 - 옵션이 있는 경우")
    void 재고복구_옵션있음() {
        // given
        Product product = Product.builder()
                .id(1L)
                .productName("등산화")
                .stockQuantity(50)
                .build();

        ProductOption option = ProductOption.builder()
                .id(1L)
                .optionType("사이즈")
                .optionValue("270")
                .stockQuantity(17) // 차감된 상태
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .product(product)
                .productOption(option)
                .quantity(3)
                .orderPrice(105000)
                .build();

        // when
        orderItem.cancel();

        // then
        assertThat(option.getStockQuantity()).isEqualTo(20); // 17 + 3 = 20 (복구)
        assertThat(product.getStockQuantity()).isEqualTo(50); // 상품 재고는 그대로
    }

    @Test
    @DisplayName("Order.cancel() 호출 시 상태 변경 및 재고 복구")
    void 주문취소_성공() {
        // given
        Product product = Product.builder()
                .id(1L)
                .productName("등산화")
                .stockQuantity(45)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .product(product)
                .productOption(null)
                .quantity(5)
                .orderPrice(100000)
                .build();

        Order order = Order.builder()
                .id(1L)
                .orderStatus(OrderStatus.PENDING)
                .orderItems(new ArrayList<>())
                .build();

        order.getOrderItems().add(orderItem);
        orderItem.setOrder(order);

        // when
        order.cancel();

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getStockQuantity()).isEqualTo(50); // 복구됨
    }

    @Test
    @DisplayName("배송중인 주문 취소 실패")
    void 주문취소_실패_배송중() {
        // given
        Order order = Order.builder()
                .id(1L)
                .orderStatus(OrderStatus.SHIPPED) // 배송중
                .orderItems(new ArrayList<>())
                .build();

        // when & then
        assertThatThrownBy(() -> order.cancel())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("배송중이거나");
    }

    @Test
    @DisplayName("배송완료된 주문 취소 실패")
    void 주문취소_실패_배송완료() {
        // given
        Order order = Order.builder()
                .id(1L)
                .orderStatus(OrderStatus.DELIVERED) // 배송완료
                .orderItems(new ArrayList<>())
                .build();

        // when & then
        assertThatThrownBy(() -> order.cancel())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("배송완료된");
    }

    @Test
    @DisplayName("상품 가격 계산 - 할인가 적용")
    void 할인가_계산() {
        // given
        Product product = Product.builder()
                .id(1L)
                .productName("등산화")
                .price(100000)
                .discountRate(20) // 20% 할인
                .build();

        // when
        Integer discountPrice = product.getDiscountPrice();

        // then
        assertThat(discountPrice).isEqualTo(80000); // 100000 * 0.8
    }

    @Test
    @DisplayName("할인율 없을 때 - 원가 반환")
    void 할인가_계산_할인없음() {
        // given
        Product product = Product.builder()
                .id(1L)
                .productName("등산화")
                .price(100000)
                .discountRate(null) // 할인 없음
                .build();

        // when
        Integer discountPrice = product.getDiscountPrice();

        // then
        assertThat(discountPrice).isEqualTo(100000); // 원가 그대로
    }

    @Test
    @DisplayName("주문 상품 총 가격 계산")
    void 주문상품_총가격_계산() {
        // given
        OrderItem orderItem = OrderItem.builder()
                .orderPrice(80000)
                .quantity(3)
                .build();

        // when
        Integer totalPrice = orderItem.getTotalPrice();

        // then
        assertThat(totalPrice).isEqualTo(240000); // 80000 * 3
    }

    @Test
    @DisplayName("주문 전체 금액 계산")
    void 주문_전체금액_계산() {
        // given
        OrderItem item1 = OrderItem.builder()
                .orderPrice(80000)
                .quantity(2)
                .build();

        OrderItem item2 = OrderItem.builder()
                .orderPrice(50000)
                .quantity(1)
                .build();

        Order order = Order.builder()
                .id(1L)
                .orderItems(new ArrayList<>())
                .build();

        order.getOrderItems().add(item1);
        order.getOrderItems().add(item2);

        // when
        order.calculateTotalPrice();

        // then
        assertThat(order.getTotalPrice()).isEqualTo(210000); // (80000*2) + (50000*1)
    }
}
