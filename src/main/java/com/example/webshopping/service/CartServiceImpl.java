package com.example.webshopping.service;

import com.example.webshopping.dto.CartDTO;
import com.example.webshopping.dto.CartItemDTO;
import com.example.webshopping.entity.*;
import com.example.webshopping.repository.CartItemRepository;
import com.example.webshopping.repository.CartRepository;
import com.example.webshopping.repository.MembersRepository;
import com.example.webshopping.repository.ProductRepository;
import com.example.webshopping.repository.ProductOptionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class CartServiceImpl  implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final MembersRepository membersRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductOptionRepository productOptionRepository;


    @Override
    public void addCart(String email, Long productId, Long productOptionId, Integer quantity) {
        //1. 사용자 확인
        Members members =
            membersRepository.findByEmail(email);

        //2. 상품 확인
        Product product =
                productRepository.findById(productId).orElseThrow(EntityNotFoundException::new);

        //3. 옵션 확인 (옵션이 있는 경우)
        ProductOption productOption = null;
        if (productOptionId != null) {
            productOption = productOptionRepository.findById(productOptionId)
                    .orElseThrow(() -> new EntityNotFoundException("옵션을 찾을 수 없습니다."));
            
            // 옵션 재고 확인
            if (quantity > productOption.getStockQuantity()){
                throw new IllegalStateException("재고가 부족합니다. (현재 재고: " + productOption.getStockQuantity() + "개)");
            }
        } else {
            // 옵션 없는 상품의 재고 확인
            if (quantity > product.getStockQuantity()){
                throw new IllegalStateException("재고가 부족합니다. (현재 재고: " + product.getStockQuantity() + "개)");
            }
        }

        //4. Cart 조회, 없으면 생성
        Cart cart = cartRepository.findByMembers_Id(members.getId()).orElseGet(() ->{
            Cart newCart = Cart.createCart(members);
            return cartRepository.save(newCart);
        });

        //5. 같은 상품+옵션이 이미 장바구니에 있는지 확인
        Optional<CartItem> existingItem;
        if (productOptionId != null) {
            existingItem = cartItemRepository.findByCart_IdAndProduct_IdAndProductOption_Id(
                    cart.getId(), productId, productOptionId);
        } else {
            existingItem = cartItemRepository.findByCart_IdAndProduct_IdAndProductOptionIsNull(
                    cart.getId(), productId);
        }
        
        if (existingItem.isPresent()){
            //기존 아이템이 있으면 수량만 증가
            CartItem cartItem = existingItem.get();
            int totalQuantity = cartItem.getQuantity() + quantity;

            //재고 초과 체크
            int availableStock = productOption != null ? 
                    productOption.getStockQuantity() : product.getStockQuantity();
            
            if (totalQuantity > availableStock) {
                throw new IllegalStateException("재고를 초과할 수 없습니다. (현재 재고: " + availableStock + "개)");
            }
            cartItem.addQuantity(quantity);
            cartItemRepository.save(cartItem);
        } else {
            //새 아이템 생성
            CartItem newCartItem = CartItem.createCartItem(cart, product, productOption, quantity);
            cart.addCartItem(newCartItem);
            cartItemRepository.save(newCartItem);
        }
    }


    @Override

    public Cart getCart(String email) {
        Members members =
            membersRepository.findByEmail(email);
        return cartRepository.findByMembers_Id(members.getId()).orElseThrow(() -> new EntityNotFoundException("장바구니가 비어있습니다."));
    }

    @Override
    public int getCartItemCount(String email) {
        try {
            Members members =
                    membersRepository.findByEmail(email);

            Optional<Cart> cart =
                    cartRepository.findByMembers_Id(members.getId());

            //장바구니가 없으면 0 반환
            if (cart.isEmpty()) {
                return 0;
            }
            return cart.get().getCartItems().size();
        } catch (EntityNotFoundException e) {
            log.warn("장바구니 개수 조회 실패 : {}", e.getMessage());

            return 0;

        }
    }

    @Override
    public CartDTO getCartDTO(String email) {
        Members members =
            membersRepository.findByEmail(email);

        Optional<Cart> cartOptional =
                cartRepository.findByMembers_Id(members.getId());

        //장바구니가 없으면 빈 CartDTO 반환
        if (cartOptional.isEmpty()) {
            return CartDTO.builder()
                    .items(new ArrayList<>())
                    .totalPrice(0)
                    .totalQuantity(0)
                    .build();
        }

        Cart cart =
            cartOptional.get();

        //CartItem을 CartItemDTO로 변환
        List<CartItemDTO> itemDTOs = cart.getCartItems().stream()
                .map(item -> {
                    Product product = item.getProduct();
                    ProductOption option = item.getProductOption();

                    return CartItemDTO.builder()
                            .cartItemId(item.getId())
                            .productId(product.getId())
                            .productName(product.getProductName())
                            .quantity(item.getQuantity())
                            .price(product.getPrice())
                            .discountRate(product.getDiscountRate())
                            .discountPrice(product.getDiscountPrice())
                            .imageUrl(product.getRepImageUrl())
                            .stockQuantity(product.getStockQuantity())
                            // 옵션 정보 추가
                            .productOptionId(option != null ? option.getId() : null)
                            .optionType(option != null ? option.getOptionType() : null)
                            .optionValue(option != null ? option.getOptionValue() : null)
                            .optionStockQuantity(option != null ? option.getStockQuantity() : null)
                            .additionalPrice(option != null ? option.getAdditionalPrice() : 0)
                            .build();
                }).toList();

        //CartDTO 생성
        CartDTO cartDTO = CartDTO.builder()
                .cartId(cart.getId())
                .items(itemDTOs)
                .build();

        //합계 계산
        cartDTO.setTotalPrice(cartDTO.calculateTotalPrice());
        cartDTO.setTotalQuantity(cartDTO.calculateTotalQuantity());

        return cartDTO;
    }

    @Override
    public void updateCartItemQuantity(Long cartItemId, Integer quantity) {

        CartItem cartItem =
            cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);

        // 재고확인 - 옵션이 있으면 옵션 재고, 없으면 상품 재고
        int availableStock;
        if (cartItem.getProductOption() != null) {
            availableStock = cartItem.getProductOption().getStockQuantity();
        } else {
            availableStock = cartItem.getProduct().getStockQuantity();
        }
        
        if (quantity > availableStock){
            throw new IllegalStateException("재고가 부족합니다. (현재 재고: " + availableStock + "개)");
        }
        
        //수량 변경
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
    }

    @Override
    public void deleteCartItem(Long cartItemId) {

        cartItemRepository.deleteById(cartItemId);

    }
}
