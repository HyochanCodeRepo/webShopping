package com.example.webshopping.service;

import com.example.webshopping.dto.ProductDTO;
import com.example.webshopping.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProductService {
    public void create(ProductDTO productDTO, List<String> imageUrls, String email);

    public void update(Long id, ProductDTO productDTO, MultipartFile mainImageFile, List<MultipartFile> detailImageFiles, Boolean keepMainImage,
                       Map<String, String> allParams);

    /*이메일로 사용자가 등록한 상품 목록 조회
     * */
    List<Product> getProductsByEmail(String email);
    
    /*모든 상품 조회 (ADMIN용)
     * */
    List<Product> getAllProducts();

    void delete(Long id);
}
