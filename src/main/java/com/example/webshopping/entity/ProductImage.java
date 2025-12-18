package com.example.webshopping.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString(exclude = "product")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private String imageUrl;
    @Column(length = 1)

    private String repImgYn;

    private Integer imageOrder;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public void setProduct(Product product) {
        this.product = product;
    }
}
