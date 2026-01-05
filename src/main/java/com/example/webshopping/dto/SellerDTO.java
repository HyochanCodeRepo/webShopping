package com.example.webshopping.dto;

import com.example.webshopping.constant.SellerStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerDTO {

    private Long id;
    private Long memberId;
    private String memberEmail;
    private String memberName;

    private String businessNumber;
    private String businessName;
    private String representativeName;
    private String businessAddress;
    private String phone;
    private String category;
    private String introduction;

    private SellerStatus status;
    private String statusDescription;

    private LocalDateTime appliedDate;
    private LocalDateTime processedDate;
    private String rejectReason;
}