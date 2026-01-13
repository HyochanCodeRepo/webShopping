package com.example.webshopping.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = {"parent", "children"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String imageUrl;

    // Self-Join: 상위 카테고리 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // 하위 카테고리 리스트
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    // 1: 대분류, 2: 중분류, 3: 소분류
    @Column(nullable = false)
    @Builder.Default
    private Integer depth = 1;

    // 카테고리 코드 (unique key로 사용 가능)
    @Column(unique = true, length = 50)
    private String code;  // 예: "SPORTS_RUN_SHOES"

    // 정렬 순서
    @Builder.Default
    private Integer displayOrder = 0;

    // 사용 여부
    @Builder.Default
    private Boolean isActive = true;
}
