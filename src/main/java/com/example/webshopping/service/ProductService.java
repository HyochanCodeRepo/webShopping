package com.example.webshopping.service;

import com.example.webshopping.dto.ProductDTO;

import java.util.List;

public interface ProductService {
    public void create(ProductDTO productDTO, List<String> imageUrls);
}
