# 🛒 webShopping (아웃도어 쇼핑몰)

> **완성도 높은 E-commerce 플랫폼 ✨ 프로젝트 완료**  
> 상품 관리부터 주문/결제, 관리자 페이지까지 실무 수준으로 구현한 종합 쇼핑몰!

<br>

[![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)](https://mariadb.org/)
[![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)

<br>

## 🆕 최근 업데이트 (2026-01-26)

### ✨ 관리자 대시보드 통계 시스템 추가

#### 📊 실시간 통계 카드 (4개)
- **오늘 매출**: 전일 대비 증감률 표시 (▲ 15.2% / ▼ 3.1%)
  - 결제완료 이상만 집계 (결제대기/취소 제외)
- **오늘 주문**: 전일 대비 증감 건수 표시
- **처리 대기**: 🔥 신규 주문 건수 (PENDING + PAYMENT_COMPLETED)
- **신규 회원**: 오늘 가입한 회원 수

#### 📈 Chart.js 차트 (2개)
- **최근 7일 매출 추이**: 꺾은선 그래프로 일별 매출 시각화
- **주문 상태 현황**: 도넛 차트로 상태별 비율 표시

#### 💡 **구현 핵심 로직**
```java
// 날짜 계산 (JPQL)
CURRENT_DATE - 1 DAY  // ✅ 올바른 문법

// 매출 집계 기준
WHERE o.orderStatus != 'CANCEL' 
  AND o.orderStatus != 'PENDING'  // 결제 대기 제외

// 처리 대기 건수
WHERE o.orderStatus IN ('PENDING', 'PAYMENT_COMPLETED')
```

### 🔔 실시간 주문 알림 뱃지 시스템 추가

#### 📍 뱃지 표시 위치
- **Header "반품 & 주문"**: 관리자/판매자만 표시
- **Admin "주문 관리" 카드**: 새 주문 건수 표시

#### 🔄 자동 갱신
- 30초마다 API 폴링으로 실시간 업데이트
- LocalStorage로 마지막 확인 시간 추적

#### 💡 **구현 핵심 로직**
```javascript
// 관리자/판매자만 뱃지 렌더링
<span sec:authorize="hasAnyRole('ADMIN', 'SELLER')">

// 30초 폴링
setInterval(updateOrderBadge, 30000);

// 뱃지 업데이트
if (count > 0) {
    badge.textContent = count > 99 ? '99+' : count;
    badge.style.display = 'flex';
}
```

#### 🎨 **CSS 스타일**
- 빨간 원형 뱃지 (#ff3b30)
- 우측 상단 절대 위치
- 99+ 오버플로우 표시

### 🗂️ 엔티티 필드 추가

#### Members 엔티티
```java
@CreationTimestamp
@Column(updatable = false)
private LocalDateTime regTime;  // 가입일시
```

#### Order 엔티티
```java
@UpdateTimestamp
private LocalDateTime updatedDate;  // 주문 수정 시간 (상태 변경 추적용)
```

### 📌 주요 의사결정 사항

1. **매출 집계 기준 변경**
   - 문제: 결제 대기 주문도 매출에 포함되는 문제
   - 해결: `PENDING` 상태 제외, 실제 결제 완료된 주문만 집계

2. **처리 대기 건수 정의**
   - 문제: 일반 주문(PENDING)이 카운트 안 됨
   - 해결: `PENDING + PAYMENT_COMPLETED` 둘 다 포함

3. **트래픽 최적화 고민**
   - 30초 폴링의 트래픽 부담 검토
   - 결론: 포트폴리오 수준에서는 문제없음 (동시 접속 100명 기준 시간당 ~7MB)

4. **뱃지 표시 대상**
   - 일반 사용자 vs 관리자 구분
   - 결론: 관리자/판매자만 표시 (일반 사용자에게는 의미 없는 정보)

---

## 📖 목차
1. [프로젝트 소개](#-프로젝트-소개)
2. [주요 기능](#-주요-기능)
3. [화면 구성](#-화면-구성)
4. [기술 스택](#️-기술-스택)
5. [프로젝트 구조](#-프로젝트-구조)
6. [핵심 구현 사항](#-핵심-구현-사항)
7. [트러블슈팅](#-트러블슈팅)
8. [개발 과정 및 느낀 점](#-개발-과정-및-느낀-점)

<br>

---

## ✨ 프로젝트 소개

### 📌 개요
- **프로젝트명**: webShopping (아웃도어 쇼핑몰)
- **개발 기간**: 2024.11 ~ 2025.01 (약 3개월)
- **개발 인원**: 1명 (개인 프로젝트)
- **개발 상태**: ✅ **1차 완료** (주요 기능 구현 완료)
- **프로젝트 목적**: 
  - 실무 수준의 E-commerce 플랫폼 구축
  - Spring Boot 생태계 전반 학습
  - 대용량 파일 업로드 & 주문 시스템 구현
  - 관리자 페이지 & 예외 처리 등 완성도 높은 개발

### 🎯 기획 배경
단순 CRUD를 넘어 **실제 운영 가능한 쇼핑몰**을 목표로, 회원 관리부터 상품 등록, 장바구니, 주문/결제, 관리자 페이지까지 전체 프로세스를 경험하고자 시작한 프로젝트입니다.

### ✅ **완료된 핵심 기능**

#### 🛍️ **사용자 기능**
- 회원가입/로그인 (Spring Security)
- 상품 검색/정렬/필터링 (가격대, 카테고리)
- 장바구니 (Session 기반)
- 주문/결제 (배송지 입력, Daum 주소 API)
- 주문 내역 조회 및 취소
- 마이페이지 (대시보드, 기본정보 수정, 주문내역)

#### 👨‍💼 **관리자 기능**
- 상품 CRUD (다중 이미지 업로드 최대 6개)
- 주문 관리 (검색/필터/페이징, 상태 변경)
- 판매자 신청 승인/거부
- 역할 기반 접근 제어 (ADMIN, SELLER, USER)

#### 🎨 **UX/UI**
- Toast 알림 시스템
- 로딩 스피너
- 반응형 디자인
- 에러 페이지 (404, 500)

<br>

---

## ✨ 주요 기능

### 👥 **1. 회원 관리**
- **회원가입 / 로그인**: 
  - Spring Security 기반 인증/인가
  - 비밀번호 암호화 (BCrypt)
  - 역할 기반 접근 제어 (ADMIN, SELLER, USER)
- **마이페이지 (리뉴얼)**:
  - 📊 대시보드: 주문 통계 (배송중/배송완료/배송준비)
  - 👤 기본정보: 이름, 전화번호, 주소 수정
  - 📦 주문내역: 전체 주문 내역 조회
  - 🔒 비밀번호 변경

### 📦 **2. 상품 관리 (관리자)**
- **상품 등록**: 
  - 대표 이미지 1개 + 상세 이미지 최대 5개 업로드
  - 카테고리, 가격, 재고, 할인율 설정
  - 파일명 중복 방지 (UUID)
- **상품 수정**: 
  - 기존 이미지 유지/삭제 선택 가능
  - 이미지 순서 변경 지원
  - 실시간 할인가 미리보기
- **상품 삭제**: 
  - Cascade 삭제로 연관 이미지 자동 삭제
  - 안전한 파일 시스템 정리

### 🛍️ **3. 상품 조회 (사용자)**
- **카테고리별 상품 목록**: 
  - 3단계 계층 구조 (대분류 → 중분류 → 소분류)
  - 페이징 처리 (20개씩)
- **검색 & 필터링**:
  - 키워드 검색 (상품명, 설명)
  - 가격대 필터 (최소가 ~ 최대가)
  - 정렬 (최신순, 가격 낮은순/높은순, 인기순)
- **상품 상세 페이지**: 
  - 대표 이미지 + 상세 이미지 갤러리
  - 재고 상태 실시간 표시
  - 장바구니 담기

### 🛒 **4. 장바구니 & 주문**
- **장바구니**: 
  - Session 기반 장바구니
  - 수량 변경, 상품 삭제
  - 선택 삭제 기능
  - 총 금액 자동 계산
- **주문/결제**:
  - Daum 주소 API 연동
  - 전화번호 자동 포맷팅
  - 약관 동의 체크
  - 주문 완료 시 재고 자동 차감
  - 장바구니 자동 비우기
- **주문 내역**:
  - 주문 목록 조회 (최신순)
  - 주문 상세 정보
  - 주문 취소 기능

### 👨‍💼 **5. 관리자 페이지 (신규)**
- **주문 관리**:
  - 🔍 통합 검색 (주문번호, 주문자명, 상품명)
  - 📅 날짜 필터 (오늘/7일/30일/직접선택)
  - 🏷️ 상태별 탭 필터 (전체/결제대기/주문확정/배송준비/배송중/배송완료/취소)
  - 🔽 정렬 (최신순, 금액 높은순/낮은순)
  - 📄 페이징 (20개씩)
  - ▶️ 다음단계 버튼 (원클릭 상태 변경)
  - 📊 상태별 카운트 실시간 표시
- **판매자 신청 관리**:
  - 신청 목록 조회
  - 승인/거부 처리
  - 거부 사유 입력

### 🎨 **6. UX 개선 (신규)**
- **Toast 알림 시스템**:
  - 성공/에러/경고/정보 4가지 타입
  - 애니메이션 효과 (슬라이드 인/아웃)
  - 자동 사라짐 (3초)
  - 클릭으로 닫기
- **예외 처리**:
  - GlobalExceptionHandler
  - CustomException + ErrorCode
  - 404/500 에러 페이지
  - 사용자 친화적 에러 메시지
- **로딩 스피너**:
  - 전체 화면 로딩 스피너
  - 폼 제출 시 자동 표시

<br>

---

## 📸 화면 구성

### 🏠 **메인 페이지**
> 카테고리별 상품 진입, 검색 기능

![메인 페이지](docs/main-page.png)

<br>

### 🛍️ **상품 목록 페이지**
> 검색/필터/정렬/페이징 기능

![상품 목록](docs/product-list-page.png)

<br>

### 🛒 **장바구니 페이지**
> 상품 수량 조절, 선택 삭제

![장바구니](docs/cart-page.png)

<br>

### 💳 **주문/결제 페이지**
> Daum 주소 API, 전화번호 포맷팅

![주문/결제](docs/checkout-page.png)

<br>

### 👤 **마이페이지 (리뉴얼)**
> 대시보드, 기본정보, 주문내역 탭

![마이페이지](docs/mypage.png)

<br>

### 👨‍💼 **관리자 주문 관리**
> 검색/필터/페이징, 다음단계 버튼

![관리자 주문 관리](docs/admin-orders.png)

<br>

---

## 🛠️ 기술 스택

### **Backend**
![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### **Frontend**
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)

### **Database**
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)

### **Build Tool**
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

### **Version Control**
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

<br>

---

## 📂 프로젝트 구조

```
webShopping/
├── src/
│   ├── main/
│   │   ├── java/com/example/webshopping/
│   │   │   ├── config/                    # 설정
│   │   │   │   ├── SecurityConfig.java       # Spring Security 설정
│   │   │   │   └── FileUploadConfig.java     # 파일 업로드 설정
│   │   │   ├── constant/                  # 상수
│   │   │   │   ├── Role.java                 # 회원 역할 (ADMIN, SELLER, USER)
│   │   │   │   └── OrderStatus.java          # 주문 상태
│   │   │   ├── controller/                # 컨트롤러
│   │   │   │   ├── MainController.java       # 메인 페이지
│   │   │   │   ├── ProductController.java    # 상품 관리
│   │   │   │   ├── CartController.java       # 장바구니
│   │   │   │   ├── OrderController.java      # 주문/결제
│   │   │   │   ├── MembersController.java    # 회원 관리
│   │   │   │   └── AdminController.java      # 관리자 페이지
│   │   │   ├── dto/                       # 데이터 전송 객체
│   │   │   │   ├── ProductDTO.java
│   │   │   │   ├── OrderRequestDTO.java
│   │   │   │   └── OrderResponseDTO.java
│   │   │   ├── entity/                    # JPA 엔티티
│   │   │   │   ├── Product.java              # 상품
│   │   │   │   ├── ProductImage.java         # 상품 이미지
│   │   │   │   ├── Category.java             # 카테고리
│   │   │   │   ├── Members.java              # 회원
│   │   │   │   ├── Cart.java                 # 장바구니
│   │   │   │   ├── CartItem.java             # 장바구니 상품
│   │   │   │   ├── Order.java                # 주문
│   │   │   │   └── OrderItem.java            # 주문 상품
│   │   │   ├── exception/                 # 예외 처리 (신규)
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── CustomException.java
│   │   │   │   └── ErrorCode.java
│   │   │   ├── repository/                # Spring Data JPA 리포지토리
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── CartRepository.java
│   │   │   │   └── MembersRepository.java
│   │   │   ├── service/                   # 비즈니스 로직
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── CartService.java
│   │   │   │   └── FileService.java
│   │   │   └── WebShoppingApplication.java
│   │   └── resources/
│   │       ├── templates/                 # Thymeleaf 템플릿
│   │       │   ├── main.html
│   │       │   ├── layout/
│   │       │   │   └── default.html          # 공통 레이아웃
│   │       │   ├── members/
│   │       │   │   ├── login.html
│   │       │   │   ├── register.html
│   │       │   │   └── mypage.html           # 마이페이지 (리뉴얼)
│   │       │   ├── product/
│   │       │   │   ├── form.html             # 상품 등록/수정
│   │       │   │   ├── list.html             # 상품 목록 (검색/필터/페이징)
│   │       │   │   └── detail.html           # 상품 상세
│   │       │   ├── cart/
│   │       │   │   └── cart.html             # 장바구니
│   │       │   ├── order/
│   │       │   │   ├── checkout.html         # 주문/결제
│   │       │   │   ├── list.html             # 주문 내역
│   │       │   │   └── detail.html           # 주문 상세
│   │       │   ├── admin/                    # 관리자 페이지 (신규)
│   │       │   │   ├── order-management.html # 주문 관리
│   │       │   │   └── seller-management.html# 판매자 관리
│   │       │   └── error/                    # 에러 페이지 (신규)
│   │       │       ├── 404.html
│   │       │       └── 500.html
│   │       ├── static/                    # 정적 리소스
│   │       │   ├── css/
│   │       │   │   └── common.css
│   │       │   └── js/
│   │       │       ├── common.js
│   │       │       └── toast.js              # Toast 알림 (신규)
│   │       └── application.properties
│   └── test/                              # 테스트 코드
└── build.gradle
```

<br>

---

## 🔥 핵심 구현 사항

### 1️⃣ **다중 이미지 업로드 시스템**

#### 📌 **ProductImage 엔티티 설계**
```java
@Entity
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    private String imageUrl;        // 이미지 저장 경로
    private String repImgYn;        // 대표 이미지 여부 (Y/N)
    private Integer imageOrder;     // 이미지 순서
}
```

#### 📌 **Product 엔티티의 이미지 관리 메서드**
```java
@Entity
public class Product {
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("imageOrder ASC")
    private List<ProductImage> images = new ArrayList<>();
    
    // 대표 이미지 URL 조회
    public String getRepImageUrl() {
        return images.stream()
                .filter(img -> "Y".equals(img.getRepImgYn()))
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }
    
    // 상세 이미지 URL 리스트 조회 (순서대로)
    public List<String> getDetailImageUrls() {
        return images.stream()
                .filter(img -> "N".equals(img.getRepImgYn()))
                .sorted(Comparator.comparing(ProductImage::getImageOrder))
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
    }
}
```

<br>

### 2️⃣ **장바구니 시스템 (Session 기반)**

#### 📌 **Cart 엔티티 설계**
```java
@Entity
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Members members;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();
    
    // 총 금액 계산
    public Integer getTotalPrice() {
        return cartItems.stream()
                .mapToInt(CartItem::getTotalPrice)
                .sum();
    }
}
```

#### 📌 **CartItem 엔티티**
```java
@Entity
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    private Integer quantity;
    
    // 상품별 총 금액 계산
    public Integer getTotalPrice() {
        return product.getDiscountPrice() * quantity;
    }
}
```

<br>

### 3️⃣ **주문 시스템**

#### 📌 **Order 엔티티 설계**
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Members member;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    private LocalDateTime orderDate;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;  // PENDING, CONFIRMED, PREPARING, SHIPPED, DELIVERED, CANCELLED
    
    private Integer totalPrice;
    
    // 배송 정보
    private String recipientName;
    private String recipientPhone;
    private String deliveryAddress;
    private String deliveryMessage;
    
    // 주문 생성 정적 팩토리 메서드
    public static Order createOrder(Members member, String recipientName, 
                                   String recipientPhone, String deliveryAddress,
                                   String deliveryMessage) {
        return Order.builder()
                .member(member)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .deliveryAddress(deliveryAddress)
                .deliveryMessage(deliveryMessage)
                .orderItems(new ArrayList<>())
                .build();
    }
    
    // 주문 취소
    public void cancel() {
        if (this.orderStatus == OrderStatus.SHIPPED || 
            this.orderStatus == OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송중이거나 배송완료된 주문은 취소할 수 없습니다.");
        }
        this.orderStatus = OrderStatus.CANCELLED;
        
        // 재고 복구
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }
}
```

#### 📌 **OrderItem 엔티티**
```java
@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    private Integer orderPrice;  // 주문 당시 가격
    private Integer quantity;
    
    // 주문 상품 생성 (재고 차감)
    public static OrderItem createOrderItem(Product product, int quantity) {
        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .orderPrice(product.getDiscountPrice())
                .quantity(quantity)
                .build();
        
        product.removeStock(quantity);  // 재고 차감
        return orderItem;
    }
    
    // 주문 취소 (재고 복구)
    public void cancel() {
        product.addStock(quantity);
    }
    
    // 주문 상품 총 가격
    public Integer getTotalPrice() {
        return orderPrice * quantity;
    }
}
```

<br>

### 4️⃣ **관리자 주문 관리 (신규)**

#### 📌 **검색/필터 쿼리**
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 관리자 주문 검색 (통합 검색 + 상태 + 날짜 필터)
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
}
```

<br>

### 5️⃣ **Toast 알림 시스템 (신규)**

#### 📌 **JavaScript 구현**
```javascript
/**
 * Toast 알림 표시
 * @param {string} message - 표시할 메시지
 * @param {string} type - 알림 타입 (success, error, warning, info)
 * @param {number} duration - 표시 시간 (ms)
 */
function showToast(message, type = 'info', duration = 3000) {
    const toast = document.createElement('div');
    toast.className = 'toast-item';
    toast.style.cssText = `
        min-width: 300px;
        padding: 16px 20px;
        background: ${typeConfig[type].bgColor};
        border-left: 4px solid ${typeConfig[type].color};
        border-radius: 8px;
        animation: slideIn 0.3s ease-out;
    `;
    
    toast.innerHTML = `
        <i class="fas ${typeConfig[type].icon}"></i>
        <span>${message}</span>
    `;
    
    container.appendChild(toast);
    
    // 자동 제거
    setTimeout(() => removeToast(toast), duration);
}
```

<br>

### 6️⃣ **예외 처리 시스템 (신규)**

#### 📌 **GlobalExceptionHandler**
```java
@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 커스텀 비즈니스 예외 처리
     */
    @ExceptionHandler(CustomException.class)
    public String handleCustomException(CustomException e, 
                                       RedirectAttributes redirectAttributes) {
        log.error("CustomException: {} - {}", e.getErrorCode().getCode(), e.getMessage());
        
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/";
    }
    
    /**
     * EntityNotFoundException 처리
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException e, Model model) {
        log.error("EntityNotFoundException: {}", e.getMessage());
        
        model.addAttribute("errorMessage", "요청하신 데이터를 찾을 수 없습니다.");
        return "error/404";
    }
    
    /**
     * 모든 예외 처리 (최종 방어선)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        log.error("Unexpected Exception: ", e);
        
        model.addAttribute("errorMessage", "서버 오류가 발생했습니다.");
        return "error/500";
    }
}
```

#### 📌 **ErrorCode Enum**
```java
@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 상품 관련
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "상품을 찾을 수 없습니다."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "P002", "재고가 부족합니다."),
    
    // 주문 관련
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다."),
    ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "O003", "취소할 수 없는 주문 상태입니다."),
    EMPTY_CART(HttpStatus.BAD_REQUEST, "O004", "장바구니가 비어있습니다."),
    
    // 회원 관련
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "M002", "이미 사용중인 이메일입니다."),
    
    // ... 기타
}
```

<br>

---

## 🚧 트러블슈팅

### 1️⃣ **문제: 장바구니 중복 상품 처리**

#### 🔴 **발생 상황**
- 같은 상품을 여러 번 담으면 CartItem이 계속 추가됨
- 사용자가 의도한 것은 수량 증가였음

#### ✅ **해결 방법**
```java
@Service
public class CartService {
    @Transactional
    public void addCart(String email, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(email);
        
        // 기존에 담긴 상품인지 확인
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // 기존 상품이면 수량만 증가
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // 새 상품이면 CartItem 생성
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
            
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            
            cart.getCartItems().add(newItem);
        }
        
        cartRepository.save(cart);
    }
}
```

#### 📌 **배운 점**
- Stream API를 활용한 중복 체크
- 비즈니스 로직을 Service 계층에서 처리

<br>

### 2️⃣ **문제: 주문 시 재고 차감 실패**

#### 🔴 **발생 상황**
- 동시에 여러 사용자가 같은 상품 주문 시 재고 오버플로우
- `@Transactional`이 제대로 동작하지 않음

#### ✅ **해결 방법**
```java
@Entity
public class Product {
    private Integer stockQuantity;
    
    // 재고 차감 (비관적 락)
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK, 
                    this.productName + "의 재고가 부족합니다. (현재 재고: " + this.stockQuantity + "개)");
        }
        this.stockQuantity = restStock;
    }
    
    // 재고 복구
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }
}
```

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}
```

#### 📌 **배운 점**
- 비관적 락(`PESSIMISTIC_WRITE`)으로 동시성 제어
- 재고 차감 전 유효성 검증 필수

<br>

### 3️⃣ **문제: 페이징 + Fetch Join 충돌**

#### 🔴 **발생 원인**
```java
// 문제가 된 코드
@Query("SELECT DISTINCT p FROM Product p " +
       "LEFT JOIN FETCH p.images " +  // Fetch Join
       "WHERE p.category.id = :categoryId")
Page<Product> findByCategoryWithImages(@Param("categoryId") Long categoryId, 
                                       Pageable pageable);

// 경고: HHH90003004: firstResult/maxResults specified with collection fetch
```

#### ✅ **해결 방법**
```java
// Fetch Join 제거 (JPA가 자동으로 LIMIT/OFFSET SQL 생성)
@Query("SELECT DISTINCT p FROM Product p " +
       "JOIN p.images " +  // FETCH 제거!
       "WHERE p.category.id = :categoryId")
Page<Product> findByCategoryWithImages(@Param("categoryId") Long categoryId, 
                                       Pageable pageable);
```

#### 📌 **배운 점**
- Fetch Join과 페이징은 함께 사용하면 안 됨
- JPA가 메모리에서 페이징하면 성능 저하
- N+1 문제는 `@BatchSize`로 해결

<br>

### 4️⃣ **문제: Order 엔티티에 orderNumber 필드 없음**

#### 🔴 **발생 원인**
```java
// Repository 쿼리
o.orderNumber LIKE %:keyword%  // ❌ orderNumber 필드가 없음!
```

#### ✅ **해결 방법**
```java
// id를 문자열로 변환해서 검색
CAST(o.id AS string) LIKE %:keyword%  // ✅ id를 주문번호로 사용
```

#### 📌 **배운 점**
- 엔티티 설계 시 필드명 확인 필수
- `CAST` 함수로 타입 변환 가능

<br>

---

## 💡 개발 과정 및 느낀 점

### 📚 **배운 점**

#### **1. JPA 연관관계 실전 활용**
- **양방향 연관관계**: Product ↔ ProductImage, Cart ↔ CartItem, Order ↔ OrderItem
- **Cascade 설정**: `CascadeType.ALL` + `orphanRemoval = true`로 연관 엔티티 자동 관리
- **Lazy Loading**: `fetch = FetchType.LAZY`로 성능 최적화
- **연관관계 편의 메서드**: `addImage()`, `addOrderItem()` 등으로 양방향 관계 안전하게 설정

#### **2. Spring Security 인증/인가**
- **역할 기반 접근 제어**: `@PreAuthorize("hasRole('ADMIN')")`
- **BCrypt 암호화**: 비밀번호 안전하게 저장
- **로그인 성공/실패 핸들러**: 커스텀 리다이렉트
- **Remember-Me 기능**: 자동 로그인 구현

#### **3. 복잡한 쿼리 작성**
- **JPQL**: 다중 조인 + 조건 + 정렬 + 페이징
- **동적 쿼리**: `:keyword IS NULL OR ...` 패턴
- **집계 함수**: `COUNT()`, `GROUP BY`로 통계 조회
- **CAST 함수**: 타입 변환으로 검색 확장

#### **4. 파일 업로드 시스템**
- **UUID**: 파일명 중복 방지
- **확장자 검증**: 허용된 이미지만 업로드
- **파일 크기 제한**: 10MB 제한
- **Cascade 삭제**: 상품 삭제 시 이미지 자동 정리

#### **5. 예외 처리 전략**
- **GlobalExceptionHandler**: 전역 예외 처리
- **CustomException**: 비즈니스 예외 정의
- **ErrorCode Enum**: 에러 코드 체계화
- **사용자 친화적 메시지**: Toast 알림으로 UX 개선

### 🎯 **성장 포인트**

#### **기술적 성장**
- **Spring Boot 생태계 이해도 향상**
- **JPA 실무 활용 능력 배양**
- **RESTful하지 않지만 실용적인 설계**
- **예외 처리 & 로깅의 중요성 체감**

#### **설계적 성장**
- **엔티티 설계**: 정규화 vs 성능 트레이드오프 고민
- **서비스 계층 분리**: 비즈니스 로직과 컨트롤러 분리
- **DTO 활용**: 엔티티와 뷰 계층 분리
- **계층별 책임 명확화**: Controller → Service → Repository

#### **완성도 향상**
- **Toast 알림**: 사용자 경험 개선
- **에러 페이지**: 404, 500 페이지 디자인
- **관리자 페이지**: 실무 수준의 검색/필터 기능
- **마이페이지 리뉴얼**: 탭 네비게이션으로 UX 개선

### 🚀 **향후 개발 방향 (선택)**

#### **인프라 개선**
```
✅ 구상 중:
- AWS S3로 이미지 업로드 전환
- Redis Session 관리 (장바구니 성능 향상)
- AWS RDS 데이터베이스 전환
- Docker 컨테이너 기반 배포
```

#### **추가 기능**
```
✅ 구상 중:
- 리뷰 시스템 (별점, 사진 리뷰)
- 찜하기 (위시리스트)
- 쿠폰 & 프로모션
- 추천 알고리즘 (구매 이력 기반)
- 재입고 알림
```

#### **성능 최적화**
```
✅ 구상 중:
- 인덱스 추가 (검색 성능 향상)
- 쿼리 최적화 (N+1 문제 해결)
- 캐시 적용 (Redis)
- 이미지 CDN 적용
```

<br>

---

## 🔗 관련 링크
- **GitHub Repository**: https://github.com/HyochanCodeRepo/webShopping
- **시연 영상**: (추가 예정)
- **배포 URL**: (추가 예정)

<br>

---

## 📧 Contact
- **이메일**: hyochan.lee91@gmail.com
- **GitHub**: https://github.com/HyochanCodeRepo

---

## 📝 License
이 프로젝트는 포트폴리오 목적으로 제작되었습니다.

---

