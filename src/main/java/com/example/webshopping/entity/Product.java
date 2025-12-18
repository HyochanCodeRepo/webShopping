package com.example.webshopping.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@ToString(exclude = "images")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(length = 1000)
    private String description;


    //    private String imageUrl;
    //이미지 리스트 추가
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("imageOrder ASC")
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();



    private Integer discountRate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;



    public Integer getDiscountPrice() {
        if(discountRate != null && discountRate > 0){
            return price - (price*discountRate/100);
        }
        return price;
    }

    public String getRepImageUrl(){
        return images.stream()
                .filter(img -> "Y".equals(img.getRepImgYn()))
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    public List<String> getDetailImageUrls(){
        return images.stream()
                .filter(img -> "N".equals(img.getRepImgYn()))
                .sorted(Comparator.comparing(ProductImage::getImageOrder))
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());

    }



    public void addImage(ProductImage image){
        images.add(image);
        image.setProduct(this);

    }



}
