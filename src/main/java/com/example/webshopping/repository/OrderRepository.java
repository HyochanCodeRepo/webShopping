package com.example.webshopping.repository;

import com.example.webshopping.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {


    /**
     * 회원 ID로 주문 목록 조회 (최신순)
     * @param memberId 회원 ID
     * @return 주문 엔티티 리스트
     */
    List<Order> findByMember_IdOrderByOrderDateDesc(Long memberId);


    // ========== 여기 추가! ==========

    /**
     * 특정 회원이 등록한 상품에 대한 주문 목록 조회 (최신순)
     * OrderItem의 Product의 Members가 해당 회원인 주문들을 조회
     * @param email 상품 등록자 이메일
     * @return 주문 엔티티 리스트
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.orderItems oi " +
            "JOIN oi.product p " +
            "WHERE p.members.email = :email " +
            "ORDER BY o.orderDate DESC")
    List<Order> findOrdersByProductOwnerEmail(@Param("email") String email);
}
