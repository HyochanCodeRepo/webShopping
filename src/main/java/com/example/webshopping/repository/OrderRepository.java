package com.example.webshopping.repository;

import com.example.webshopping.constant.OrderStatus;
import com.example.webshopping.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {


    /**
     * 회원 ID로 주문 목록 조회 (최신순)
     * @param memberId 회원 ID
     * @return 주문 엔티티 리스트
     */
    List<Order> findByMember_IdOrderByOrderDateDesc(Long memberId);
    
    /**
     * 토스 주문 ID로 주문 조회 (결제용)
     * @param orderId 토스 주문 ID (UUID)
     * @return 주문 엔티티
     */
    Optional<Order> findByOrderId(String orderId);


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
    
    
    // ========== 관리자 주문 검색/필터 ==========
    
    /**
     * 관리자 주문 검색 (통합 검색 + 상태 + 날짜 필터) - 최신순
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE p.members.email = :email " +
           "AND (:keyword IS NULL OR " +
           "     CAST(o.id AS string) LIKE %:keyword% OR " +
           "     o.member.name LIKE %:keyword% OR " +
           "     p.productName LIKE %:keyword%) " +
           "AND (:status IS NULL OR o.orderStatus = :status) " +
           "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
           "AND (:endDate IS NULL OR o.orderDate <= :endDate) " +
           "ORDER BY o.orderDate DESC")
    Page<Order> searchOrdersLatest(
        @Param("email") String email,
        @Param("keyword") String keyword,
        @Param("status") OrderStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * 관리자 주문 검색 - 금액 높은순
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE p.members.email = :email " +
           "AND (:keyword IS NULL OR " +
           "     CAST(o.id AS string) LIKE %:keyword% OR " +
           "     o.member.name LIKE %:keyword% OR " +
           "     p.productName LIKE %:keyword%) " +
           "AND (:status IS NULL OR o.orderStatus = :status) " +
           "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
           "AND (:endDate IS NULL OR o.orderDate <= :endDate) " +
           "ORDER BY o.totalPrice DESC")
    Page<Order> searchOrdersByAmountDesc(
        @Param("email") String email,
        @Param("keyword") String keyword,
        @Param("status") OrderStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * 관리자 주문 검색 - 금액 낮은순
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE p.members.email = :email " +
           "AND (:keyword IS NULL OR " +
           "     CAST(o.id AS string) LIKE %:keyword% OR " +
           "     o.member.name LIKE %:keyword% OR " +
           "     p.productName LIKE %:keyword%) " +
           "AND (:status IS NULL OR o.orderStatus = :status) " +
           "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
           "AND (:endDate IS NULL OR o.orderDate <= :endDate) " +
           "ORDER BY o.totalPrice ASC")
    Page<Order> searchOrdersByAmountAsc(
        @Param("email") String email,
        @Param("keyword") String keyword,
        @Param("status") OrderStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * 상태별 주문 개수 조회 (통계용)
     */
    @Query("SELECT o.orderStatus, COUNT(o) FROM Order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE p.members.email = :email " +
           "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
           "AND (:endDate IS NULL OR o.orderDate <= :endDate) " +
           "GROUP BY o.orderStatus")
    List<Object[]> countOrdersByStatus(
        @Param("email") String email,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // ========== 관리자 대시보드 통계 (ADMIN 전체 데이터) ==========
    
    /**
     * 오늘 매출 합계 (결제완료 이상만 포함, 취소/결제대기 제외)
     */
    @Query(value = "SELECT COALESCE(SUM(o.total_price), 0) FROM orders o " +
           "WHERE DATE(o.order_date) = CURRENT_DATE " +
           "AND o.order_status != 'CANCELLED' " +
           "AND o.order_status != 'PENDING'", nativeQuery = true)
    Integer getTodaySales();
    
    /**
     * 어제 매출 합계 (전일 대비 계산용)
     */
    @Query(value = "SELECT COALESCE(SUM(o.total_price), 0) FROM orders o " +
           "WHERE DATE(o.order_date) = CURRENT_DATE - INTERVAL 1 DAY " +
           "AND o.order_status != 'CANCELLED' " +
           "AND o.order_status != 'PENDING'", nativeQuery = true)
    Integer getYesterdaySales();
    
    /**
     * 오늘 주문 건수
     */
    @Query(value = "SELECT COUNT(*) FROM orders o " +
           "WHERE DATE(o.order_date) = CURRENT_DATE", nativeQuery = true)
    Long getTodayOrderCount();
    
    /**
     * 어제 주문 건수 (전일 대비 계산용)
     */
    @Query(value = "SELECT COUNT(*) FROM orders o " +
           "WHERE DATE(o.order_date) = CURRENT_DATE - INTERVAL 1 DAY", nativeQuery = true)
    Long getYesterdayOrderCount();
    
    /**
     * 처리 대기 중인 주문 건수 (주문확정 + 결제대기 상태)
     */
    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.orderStatus IN ('PENDING', 'CONFIRMED')")
    Long getPendingOrderCount();
    
    /**
     * 최근 7일 일별 매출 (차트용, 결제완료 이상만 포함)
     * @return [날짜, 매출] 형태의 리스트
     */
    @Query(value = "SELECT DATE(o.order_date), COALESCE(SUM(o.total_price), 0) " +
           "FROM orders o " +
           "WHERE o.order_date >= CURRENT_DATE - INTERVAL 6 DAY " +
           "AND o.order_status != 'CANCELLED' " +
           "AND o.order_status != 'PENDING' " +
           "GROUP BY DATE(o.order_date) " +
           "ORDER BY DATE(o.order_date) ASC", nativeQuery = true)
    List<Object[]> getLast7DaysSales();
    
    /**
     * 전체 주문 상태별 건수 (도넛 차트용)
     * @return [상태, 건수] 형태의 리스트
     */
    @Query("SELECT o.orderStatus, COUNT(o) FROM Order o " +
           "GROUP BY o.orderStatus")
    List<Object[]> getOrderStatusCounts();
    
    /**
     * 특정 회원의 특정 시간 이후 상태 변경된 주문 건수
     * (일반 사용자용 - 새 알림 체크)
     * @param memberId 회원 ID
     * @param lastCheckedTime 마지막 확인 시간
     * @return 상태 변경된 주문 건수
     */
    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.member.id = :memberId " +
           "AND o.updatedDate > :lastCheckedTime")
    Long countUpdatedOrdersByMember(@Param("memberId") Long memberId, 
                                      @Param("lastCheckedTime") LocalDateTime lastCheckedTime);
}
