package com.example.webshopping.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product_detail")
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_detail_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    /**
     * 상세 설명 HTML (큰 textarea로 입력)
     */
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String detailHtml;

    /**
     * 추가 상세 이미지 URL 목록
     */
    @ElementCollection
    @CollectionTable(name = "product_detail_images", joinColumns = @JoinColumn(name = "product_detail_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> detailImages = new ArrayList<>();
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * 상세 이미지 추가
     */
    public void addDetailImage(String imageUrl) {
        if (this.detailImages == null) {
            this.detailImages = new ArrayList<>();
        }
        this.detailImages.add(imageUrl);
    }
}
