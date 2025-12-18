package com.example.webshopping.service;

import com.example.webshopping.dto.CategoryDTO;
import com.example.webshopping.dto.ProductDTO;
import com.example.webshopping.entity.Category;
import com.example.webshopping.entity.Product;
import com.example.webshopping.entity.ProductImage;
import com.example.webshopping.repository.CategoryRepository;
import com.example.webshopping.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {


    private final ProductRepository productRepository;
    private final ModelMapper modelMapper = new ModelMapper();
    private final CategoryRepository categoryRepository;



    @Override
    public void create(ProductDTO productDTO, List<String> imageUrls) {

        Product product = Product.builder()
                .productName(productDTO.getProductName())
                .price(productDTO.getPrice())
                .stockQuantity(productDTO.getStockQuantity())
                .description(productDTO.getDescription())
                .discountRate(productDTO.getDiscountRate())
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

        productRepository.save(product);


    }
}
