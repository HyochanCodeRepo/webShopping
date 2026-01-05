package com.example.webshopping.service;

import com.example.webshopping.dto.SellerDTO;

import java.util.List;

public interface SellerService {

    /**
     * 판매자 신청 제출
     */
    Long submitApplication(String email, SellerDTO dto);

    /**
     * 내 신청 내역 조회
     */
    List<SellerDTO> getMyApplications(String email);

    /**
     * 승인 대기 중인 신청 목록 조회 (관리자용)
     */
    List<SellerDTO> getPendingApplications();

    /**
     * 모든 신청 목록 조회 (관리자용)
     */
    List<SellerDTO> getAllApplications();

    /**
     * 신청 상세 조회
     */
    SellerDTO getApplication(Long sellerId);

    /**
     * 신청 승인
     */
    void approveApplication(Long sellerId);

    /**
     * 신청 거부
     */
    void rejectApplication(Long sellerId, String reason);

    /**
     * 승인 대기 중인 신청이 있는지 확인
     */
    boolean hasPendingApplication(String email);
    
    /**
     * 판매자 신청 수정
     */
    void updateApplication(Long sellerId, SellerDTO dto);
}