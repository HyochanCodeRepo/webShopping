package com.example.webshopping.entity;

import com.example.webshopping.constant.SellerStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Members member;

    @Column(nullable = false, length = 12)
    private String businessNumber;      // 사업자등록번호 (123-45-67890)

    @Column(nullable = false, length = 100)
    private String businessName;        // 상호명

    @Column(nullable = false, length = 50)
    private String representativeName;  // 대표자명

    @Column(nullable = false, length = 200)
    private String businessAddress;     // 사업장 주소

    @Column(nullable = false, length = 20)
    private String phone;               // 연락처

    @Column(nullable = false, length = 50)
    private String category;            // 판매 카테고리

    @Column(length = 1000)
    private String introduction;        // 판매 계획 및 소개

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerStatus status;        // 신청 상태

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedDate;  // 신청일

    private LocalDateTime processedDate; // 처리일 (승인/거부)

    @Column(length = 500)
    private String rejectReason;        // 거부 사유

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = SellerStatus.PENDING;
        }
    }
}