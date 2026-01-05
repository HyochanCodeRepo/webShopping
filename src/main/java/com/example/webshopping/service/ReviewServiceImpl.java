package com.example.webshopping.service;

import com.example.webshopping.dto.ReviewDTO;
import com.example.webshopping.entity.Members;
import com.example.webshopping.entity.Product;
import com.example.webshopping.entity.Review;
import com.example.webshopping.repository.MembersRepository;
import com.example.webshopping.repository.ProductRepository;
import com.example.webshopping.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final MembersRepository membersRepository;
    private final FileService fileService;

    @Override
    public void createReview(ReviewDTO reviewDTO, MultipartFile imageFile, String email) {
        // 회원 조회
        Members member = membersRepository.findByEmail(email);
        if (member == null) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다.");
        }

        // 상품 조회
        Product product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        // 이미 리뷰 작성했는지 체크
        if (reviewRepository.existsByProduct_IdAndMember_Id(product.getId(), member.getId())) {
            throw new IllegalStateException("이미 리뷰를 작성하셨습니다.");
        }

        // 이미지 업로드 (선택사항)
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = fileService.uploadFile(imageFile);
        }

        // 리뷰 생성
        Review review = Review.builder()
                .product(product)
                .member(member)
                .rating(reviewDTO.getRating())
                .content(reviewDTO.getContent())
                .imageUrl(imageUrl)
                .build();

        reviewRepository.save(review);
        log.info("리뷰 작성 완료 - Product: {}, Member: {}, Rating: {}", 
                product.getId(), member.getEmail(), reviewDTO.getRating());
    }

    @Override
    public void updateReview(Long reviewId, ReviewDTO reviewDTO, MultipartFile imageFile, String email) {
        // 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!review.getMember().getEmail().equals(email)) {
            throw new IllegalStateException("본인의 리뷰만 수정할 수 있습니다.");
        }

        // 리뷰 수정
        review.setRating(reviewDTO.getRating());
        review.setContent(reviewDTO.getContent());

        // 이미지 업로드 (새 이미지가 있으면)
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileService.uploadFile(imageFile);
            review.setImageUrl(imageUrl);
        }

        reviewRepository.save(review);
        log.info("리뷰 수정 완료 - Review ID: {}", reviewId);
    }

    @Override
    public void deleteReview(Long reviewId, String email) {
        // 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!review.getMember().getEmail().equals(email)) {
            throw new IllegalStateException("본인의 리뷰만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
        log.info("리뷰 삭제 완료 - Review ID: {}", reviewId);
    }

    @Override
    public ReviewDTO getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));
        
        return convertToDTO(review);
    }

    @Override
    public List<ReviewDTO> getReviewsByProductId(Long productId) {
        List<Review> reviews = reviewRepository.findByProduct_IdOrderByCreatedAtDesc(productId);

        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getMyReviews(String email) {
        Members member = membersRepository.findByEmail(email);
        if (member == null) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다.");
        }

        List<Review> reviews = reviewRepository.findByMember_IdOrderByCreatedAtDesc(member.getId());

        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.getAverageRatingByProductId(productId);
        return avg != null ? Math.round(avg * 10) / 10.0 : 0.0; // 소수점 첫째자리
    }

    @Override
    public Long getReviewCount(Long productId) {
        return reviewRepository.countByProduct_Id(productId);
    }

    @Override
    public boolean canWriteReview(Long productId, String email) {
        Members member = membersRepository.findByEmail(email);
        if (member == null) {
            return false;
        }

        // 이미 리뷰 작성했으면 false
        if (reviewRepository.existsByProduct_IdAndMember_Id(productId, member.getId())) {
            return false;
        }

        // 지금은 누구나 작성 가능
        return true;
    }

    /**
     * Entity -> DTO 변환
     */
    private ReviewDTO convertToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getProductName())
                .productImageUrl(review.getProduct().getRepImageUrl())
                .memberId(review.getMember().getId())
                .memberName(review.getMember().getName())
                .rating(review.getRating())
                .content(review.getContent())
                .imageUrl(review.getImageUrl())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
