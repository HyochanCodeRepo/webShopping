# 🛒 webShopping

> **Spring Boot 기반 아웃도어 E-commerce 플랫폼**  
> 실무 수준의 상품 관리, 주문 시스템, 관리자 대시보드 구현

[![Java](https://img.shields.io/badge/Java_17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5.3-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=flat-square&logo=mariadb&logoColor=white)](https://mariadb.org/)
[![AWS EC2](https://img.shields.io/badge/AWS_EC2-FF9900?style=flat-square&logo=amazonec2&logoColor=white)](https://aws.amazon.com/ec2/)

🔗 **[배포 링크](http://52.78.152.205:8080/)** | 📧 **hyochan.lee91@gmail.com**

---

## 📌 프로젝트 개요

| 항목 | 내용                      |
|------|-------------------------|
| **개발 기간** | 2024.12 ~ 2025.01 (45일) |
| **개발 인원** | 1명 (개인 프로젝트)            |
| **배포 환경** | AWS EC2 (프리티어)          |

### 🎯 **핵심 목표**
단순 CRUD를 넘어 **실무 수준의 복잡한 비즈니스 로직**을 구현한 E-commerce 플랫폼

---

## 🛠️ 기술 스택

### **Backend**
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.3, Spring Data JPA, Spring Security
- **ORM**: Hibernate, JPQL (복잡한 쿼리)
- **Database**: MariaDB
- **Library**: 
  - Lombok (코드 간소화)
  - ModelMapper (Entity ↔ DTO 변환)
  - Log4j2 (로깅)
- **Build Tool**: Gradle

### **Frontend**
- **Template Engine**: Thymeleaf
- **JavaScript**: ES6, Chart.js (통계 차트)
- **API**: Daum 주소 API (주소 검색)
- **UX**: Toast 알림, 로딩 스피너, 실시간 뱃지

### **Security**
- Spring Security (인증/인가)
- BCrypt (비밀번호 암호화)
- CSRF 방어
- RBAC (역할 기반 접근 제어)

### **Infrastructure**
- **Deploy**: AWS EC2 (Ubuntu 24.04)
- **Database**: MariaDB (EC2 내부)
- **File Storage**: Local File System + UUID (중복 방지)

---

## ✨ 주요 기능

### 👤 **회원 & 인증**
- Spring Security 기반 인증/인가, 역할 기반 접근 제어 (ADMIN, SELLER, USER)

### 📦 **상품 관리**
- 상품 CRUD, 카테고리 3단계 계층 구조, 검색/필터/정렬/페이징

### 🛒 **주문 시스템**
- 장바구니 (Session), 주문/결제, 주문 내역 조회 및 취소

### 👨‍💼 **관리자**
- 실시간 매출 통계 대시보드, 주문 관리 (검색/필터/상태 변경), 판매자 승인 관리

---

## 🔥 핵심 기술 구현

### **1. 다중 이미지 업로드 시스템**

```java
// OneToMany 연관관계 + Cascade로 이미지 자동 관리
@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
@OrderBy("imageOrder ASC")
private List<ProductImage> images = new ArrayList<>();

// 연관관계 편의 메서드
public void addImage(ProductImage image) {
    images.add(image);
    image.setProduct(this);
}
```

**구현 포인트**:
- 대표 이미지 1개 + 상세 이미지 최대 5개
- `orphanRemoval = true`로 상품 삭제 시 이미지 자동 삭제
- UUID로 파일명 중복 방지 (`UUID.randomUUID().toString()`)
- `imageOrder` 필드로 정렬 순서 관리
- `MultipartFile` → 로컬 저장 (`C:/ex/product-images`)

**사용 기술**: `@OneToMany`, `CascadeType.ALL`, `orphanRemoval`, `UUID`, `MultipartFile`

---

### **2. 상품 옵션 시스템 (사이즈, 색상)**

```java
@Entity
public class ProductOption {
    private String optionType;        // "사이즈", "색상"
    private String optionValue;       // "M", "블랙"
    private Integer stockQuantity;    // 옵션별 재고
    private Integer additionalPrice;  // 추가 금액
}

// 주문 시 옵션별 재고 차감
if (productOption != null) {
    productOption.setStockQuantity(
        productOption.getStockQuantity() - quantity
    );
}
```

**구현 포인트**:
- 옵션별 독립적인 재고 관리
- 옵션 미선택 시 상품 기본 재고 사용
- 품절 옵션 선택 불가 처리
- 장바구니/주문 전체 프로세스에 옵션 정보 전달
- `@ManyToOne` 연관관계로 Product와 연결

**사용 기술**: `@ManyToOne`, `FetchType.LAZY`, `@Builder.Default`

---

### **3. 동시성 제어 (재고 관리)**

```java
// Repository: 비관적 락으로 동시 접근 제어
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdWithLock(@Param("id") Long id);

// Entity: 재고 부족 시 예외 발생
public void removeStock(int quantity) {
    int restStock = this.stockQuantity - quantity;
    if (restStock < 0) {
        throw new CustomException(ErrorCode.OUT_OF_STOCK);
    }
    this.stockQuantity = restStock;
}
```

**구현 포인트**:
- **비관적 락**(`PESSIMISTIC_WRITE`)으로 동시 주문 시 재고 오버플로우 방지
- `@Transactional` 내에서 재고 검증 → 차감 → 주문 생성 순서 보장
- 주문 취소 시 `addStock()`으로 재고 자동 복구
- `CustomException` + `ErrorCode`로 예외 처리

**사용 기술**: `@Lock`, `PESSIMISTIC_WRITE`, `@Transactional`, Custom Exception

---

### **4. 계층형 카테고리 (재귀 쿼리)**

```java
// Self-Join으로 무한 depth 카테고리 구현
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private Category parent;

@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
private List<Category> children = new ArrayList<>();

// 대분류 클릭 시 모든 하위 카테고리 상품 조회
@Query("SELECT p FROM Product p " +
       "WHERE p.category.id = :categoryId " +
       "OR p.category.parent.id = :categoryId " +
       "OR p.category.parent.parent.id = :categoryId")
Page<Product> findByCategoryWithHierarchy(@Param("categoryId") Long categoryId);
```

**구현 포인트**:
- **Self-Join**으로 대분류/중분류/소분류 3단계 구조
- 상위 카테고리 클릭 시 하위 카테고리 상품 모두 조회
- `depth` 필드로 계층 레벨 관리
- `displayOrder` 필드로 정렬 순서 제어

**사용 기술**: Self-Join, JPQL 3단계 OR 조건, `Page<T>` (페이징)

---

### **5. 관리자 대시보드 (통계 & 차트)**

```java
// 오늘 매출 (전일 대비 증감률 계산)
@Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o " +
       "WHERE DATE(o.orderDate) = CURRENT_DATE " +
       "AND o.orderStatus != 'CANCEL'")
Integer getTodaySales();

// 최근 7일 매출 추이
@Query("SELECT DATE(o.orderDate), SUM(o.totalPrice) FROM Order o " +
       "WHERE o.orderDate >= :startDate " +
       "GROUP BY DATE(o.orderDate) " +
       "ORDER BY DATE(o.orderDate)")
List<Object[]> getSalesLast7Days(@Param("startDate") LocalDateTime startDate);
```

**구현 포인트**:
- 실시간 통계 카드 4개 (오늘 매출, 주문, 처리 대기, 신규 회원)
- **Chart.js**로 데이터 시각화 (꺾은선, 도넛 차트)
- 전일 대비 증감률 계산 및 표시 (▲ 15.2% / ▼ 3.1%)
- **30초 폴링**으로 신규 주문 뱃지 자동 갱신
- `COALESCE()`, `DATE()`, `GROUP BY` 등 SQL 집계 함수 활용

**사용 기술**: JPQL 집계 쿼리, Chart.js, JavaScript `setInterval()`, LocalStorage

---

### **6. 예외 처리 전략**

```java
// ErrorCode Enum으로 에러 체계화
public enum ErrorCode {
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "P002", "재고가 부족합니다."),
    ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "O003", "취소할 수 없는 주문 상태입니다.");
}

// GlobalExceptionHandler로 전역 예외 처리
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public String handleCustomException(CustomException e, 
                                       RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/";
    }
}
```

**구현 포인트**:
- **비즈니스 예외**와 **시스템 예외** 분리
- `ErrorCode` Enum으로 HTTP 상태, 코드, 메시지 통합 관리
- `@ControllerAdvice`로 전역 예외 처리
- **Toast 알림**으로 사용자 친화적 메시지 전달
- 404/500 커스텀 에러 페이지

**사용 기술**: `@ControllerAdvice`, `@ExceptionHandler`, `RedirectAttributes`, Custom Exception, Enum

---

### **7. 복잡한 검색/필터 쿼리**

```java
// 동적 쿼리 (키워드 + 카테고리 + 가격대 + 정렬)
@Query("SELECT DISTINCT p FROM Product p " +
       "JOIN p.orderItems oi " +
       "WHERE (:keyword IS NULL OR p.productName LIKE %:keyword%) " +
       "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
       "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
       "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
       "GROUP BY p.id " +
       "ORDER BY COUNT(oi) DESC")
Page<Product> searchOrderByPopular(...);
```

**구현 포인트**:
- 다중 조건 **동적 쿼리** (`:param IS NULL OR ...` 패턴)
- 페이징 + 정렬 조합 (최신순, 가격순, 인기순)
- JPQL로 복잡한 집계 쿼리 작성
- `DISTINCT`로 중복 제거
- `Pageable`로 페이지 크기 20개 제어

**사용 기술**: JPQL 동적 쿼리, `@Param`, `Page<T>`, `Pageable`, `COUNT()`, `GROUP BY`

---

## 🏗️ 아키텍처

```
┌─────────────┐
│   Client    │
│ (Thymeleaf) │
└──────┬──────┘
       │
┌──────▼──────────────┐
│   Controller        │
│  (REST/MVC 혼합)    │
└──────┬──────────────┘
       │
┌──────▼──────────────┐
│   Service           │
│ (비즈니스 로직)      │
└──────┬──────────────┘
       │
┌──────▼──────────────┐
│   Repository        │
│  (Spring Data JPA)  │
└──────┬──────────────┘
       │
┌──────▼──────────────┐
│   MariaDB           │
└─────────────────────┘
```

**설계 원칙**:
- 계층별 책임 명확하게 분리
- Entity ↔ DTO 변환으로 계층 간 의존성 차단
- Service 계층에 `@Transactional` 적용

---

## 📊 ERD (주요 테이블)

```
Members (회원)
  ├─ 1:N → Product (상품)
  ├─ 1:1 → Cart (장바구니)
  └─ 1:N → Order (주문)

Product (상품)
  ├─ 1:N → ProductImage (이미지)
  ├─ 1:N → ProductOption (옵션)
  ├─ N:1 → Category (카테고리)
  └─ 1:N → OrderItem (주문 상품)

Order (주문)
  └─ 1:N → OrderItem (주문 상품)

Category (카테고리)
  └─ Self-Join (계층 구조)
```

---

## 🚀 배포

### **환경**
- **서버**: AWS EC2 (t2.micro, Ubuntu 24.04)
- **데이터베이스**: MariaDB (EC2 내부)
- **파일 저장소**: 로컬 파일 시스템

### **배포 방법**
```bash
# JAR 빌드
./gradlew clean build

# EC2 업로드
scp build/libs/*.jar ubuntu@ec2-ip:/home/ubuntu/

# 백그라운드 실행
nohup java -jar -Dspring.profiles.active=prod webShopping.jar &
```

---

## 🎯 트러블슈팅

### **1. 페이징 + Fetch Join 충돌**
**문제**: `@OneToMany` + `JOIN FETCH` + `Pageable` 사용 시 경고 발생  
**해결**: Fetch Join 제거 → JPA가 자동으로 LIMIT/OFFSET 생성하도록 변경

### **2. 주문 시 재고 오버플로우**
**문제**: 동시 주문 시 재고가 마이너스가 되는 문제  
**해결**: `@Lock(PESSIMISTIC_WRITE)`로 비관적 락 적용

### **3. 장바구니 중복 상품 처리**
**문제**: 같은 상품을 여러 번 담으면 CartItem이 계속 추가됨  
**해결**: Stream API로 기존 상품 확인 후 수량만 증가

---

## 💡 개발 과정에서 배운 점

### **기술적 성장**
- JPA 연관관계 실전 활용 (양방향, Cascade, Lazy Loading)
- 비관적 락으로 동시성 제어 경험
- JPQL 복잡한 쿼리 작성 능력 향상
- 계층형 아키텍처 설계 경험

### **설계적 성장**
- 엔티티 설계 시 정규화 vs 성능 트레이드오프 고민
- 예외 처리 전략 수립 (ErrorCode 체계화)
- 사용자 경험 개선 (Toast 알림, 로딩 스피너)

---

## 📂 프로젝트 구조

```
src/main/java/com/example/webshopping/
├── config/          # Security, File Upload 설정
├── constant/        # Enum (Role, OrderStatus, ProductType 등)
├── controller/      # MVC 컨트롤러
├── dto/             # 데이터 전송 객체
├── entity/          # JPA 엔티티
├── exception/       # 예외 처리 (GlobalExceptionHandler, ErrorCode)
├── repository/      # Spring Data JPA 리포지토리
└── service/         # 비즈니스 로직

src/test/java/com/example/webshopping/
├── service/         # Service 계층 단위 테스트
└── repository/      # Repository 통합 테스트
```

---

## 🧪 테스트

- **테스트 프레임워크**: JUnit 5, Mockito, AssertJ
- **테스트 범위**:
  - Service 계층 단위 테스트 (비즈니스 로직 검증)
  - Repository 통합 테스트 (복잡한 쿼리 검증)
  - 재고 차감, 주문 취소 등 핵심 로직 테스트
- **위치**: `src/test/java/com/example/webshopping`

---

## 🔗 Links

- **GitHub**: https://github.com/HyochanCodeRepo/webShopping
- **배포 URL**: http://43.201.22.151:8080/
- **개발자**: 이효찬 (hyochan.lee91@gmail.com)

---

## 📝 License

이 프로젝트는 포트폴리오 목적으로 제작되었습니다.
