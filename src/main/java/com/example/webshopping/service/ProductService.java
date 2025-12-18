package com.example.webshopping.service;

import com.example.webshopping.dto.ProductDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProductService {
    public void create(ProductDTO productDTO, List<String> imageUrls);

    public void update(Long id, ProductDTO productDTO, MultipartFile mainImageFile, List<MultipartFile> detailImageFiles, Boolean keepMainImage,
                       Map<String, String> allParams);
}
