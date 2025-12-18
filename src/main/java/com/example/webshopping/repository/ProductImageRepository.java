package com.example.webshopping.repository;


import com.example.webshopping.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    //특정 상품의 이미지를 조회
    List<ProductImage> findByProduct_IdOrderByImageOrderAsc(Long productId);

    //특정 상품의 대표 이미지 조회
    ProductImage findByProduct_IdAndRepImgYn(Long productId, String repImgYn);

    //특정 상품의 이미지 삭제
    void deleteByProduct_Id(Long productId);

}
