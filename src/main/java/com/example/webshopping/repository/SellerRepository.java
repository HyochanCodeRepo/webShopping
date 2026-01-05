package com.example.webshopping.repository;

import com.example.webshopping.constant.SellerStatus;
import com.example.webshopping.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    /**
     * 회원 ID로 신청 내역 조회 (최신순)
     */
    List<Seller> findByMember_IdOrderByAppliedDateDesc(Long memberId);

    /**
     * 상태별 신청 목록 조회 (최신순)
     */
    List<Seller> findByStatusOrderByAppliedDateDesc(SellerStatus status);

    /**
     * 회원의 승인 대기 중인 신청 존재 여부
     */
    boolean existsByMember_IdAndStatus(Long memberId, SellerStatus status);

    /**
     * 특정 회원의 가장 최근 신청 조회
     */
    Optional<Seller> findTopByMember_IdOrderByAppliedDateDesc(Long memberId);
}