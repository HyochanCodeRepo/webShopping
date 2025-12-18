package com.example.webshopping.service;

import com.example.webshopping.dto.CategoryDTO;
import com.example.webshopping.dto.ProductDTO;
import com.example.webshopping.entity.Category;
import com.example.webshopping.entity.Product;
import com.example.webshopping.repository.CategoryRepository;
import com.example.webshopping.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper = new ModelMapper();
    private final CategoryRepository categoryRepository;
    @Override
    public void create(ProductDTO productDTO) {

        Product product =
            modelMapper.map(productDTO, Product.class);

        Category category =
                categoryRepository.findById(productDTO.getCategoryId()).orElseThrow(EntityNotFoundException::new);
        product.setCategory(category);


        productRepository.save(product);


    }
}
