package com.example.webshopping.repository;

import com.example.webshopping.entity.Members;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MembersRepository extends JpaRepository<Members, Long> {

    public Members findByEmail(String email);
    
    // ========== 관리자 대시보드 통계 ==========
    
    /**
     * 오늘 신규 가입 회원 수
     */
    @Query(value = "SELECT COUNT(*) FROM members WHERE DATE(reg_time) = CURRENT_DATE", nativeQuery = true)
    Long getTodayNewMemberCount();

    /**
     * 어제 신규 가입 회원 수 (전일 대비 계산용)
     */
    @Query(value = "SELECT COUNT(*) FROM members WHERE DATE(reg_time) = CURRENT_DATE - INTERVAL 1 DAY", nativeQuery = true)
    Long getYesterdayNewMemberCount();

}
