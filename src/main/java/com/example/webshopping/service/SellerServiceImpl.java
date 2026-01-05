package com.example.webshopping.service;

import com.example.webshopping.constant.Role;
import com.example.webshopping.constant.SellerStatus;
import com.example.webshopping.dto.SellerDTO;
import com.example.webshopping.entity.Members;
import com.example.webshopping.entity.Seller;
import com.example.webshopping.repository.MembersRepository;
import com.example.webshopping.repository.SellerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final MembersRepository membersRepository;

    @Override
    public Long submitApplication(String email, SellerDTO dto) {
        // 회원 조회
        Members member = membersRepository.findByEmail(email);
        if (member == null) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다.");
        }

        // 이미 판매자인지 확인
        if (member.getRole() == Role.ROLE_SELLER || member.getRole() == Role.ROLE_ADMIN) {
            throw new IllegalStateException("이미 판매자 권한을 가지고 있습니다.");
        }

        // 승인 대기 중인 신청이 있는지 확인
        if (sellerRepository.existsByMember_IdAndStatus(member.getId(), SellerStatus.PENDING)) {
            throw new IllegalStateException("이미 승인 대기 중인 신청이 있습니다.");
        }

        // 신청서 생성
        Seller seller = Seller.builder()
                .member(member)
                .businessNumber(dto.getBusinessNumber())
                .businessName(dto.getBusinessName())
                .representativeName(dto.getRepresentativeName())
                .businessAddress(dto.getBusinessAddress())
                .phone(dto.getPhone())
                .category(dto.getCategory())
                .introduction(dto.getIntroduction())
                .status(SellerStatus.PENDING)
                .build();

        Seller saved = sellerRepository.save(seller);

        log.info("판매자 신청 제출 - 회원: {}, 신청ID: {}", email, saved.getId());

        return saved.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerDTO> getMyApplications(String email) {
        Members member = membersRepository.findByEmail(email);
        if (member == null) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다.");
        }

        List<Seller> sellers =
                sellerRepository.findByMember_IdOrderByAppliedDateDesc(member.getId());

        return sellers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerDTO> getPendingApplications() {
        List<Seller> sellers =
                sellerRepository.findByStatusOrderByAppliedDateDesc(SellerStatus.PENDING);

        return sellers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerDTO> getAllApplications() {
        List<Seller> sellers = sellerRepository.findAll();

        return sellers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SellerDTO getApplication(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("신청 내역을 찾을 수 없습니다."));

        return convertToDTO(seller);
    }

    @Override
    public void approveApplication(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("신청 내역을 찾을 수 없습니다."));

        // 이미 처리된 신청인지 확인
        if (seller.getStatus() != SellerStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        // 신청 상태 변경
        seller.setStatus(SellerStatus.APPROVED);
        seller.setProcessedDate(LocalDateTime.now());

        // 회원 권한 변경: USER → SELLER
        Members member = seller.getMember();
        member.setRole(Role.ROLE_SELLER);
        membersRepository.save(member);

        sellerRepository.save(seller);

        log.info("판매자 신청 승인 - 신청ID: {}, 회원: {}", sellerId, member.getEmail());
    }

    @Override
    public void rejectApplication(Long sellerId, String reason) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("신청 내역을 찾을 수 없습니다."));

        // 이미 처리된 신청인지 확인
        if (seller.getStatus() != SellerStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        // 신청 상태 변경
        seller.setStatus(SellerStatus.REJECTED);
        seller.setProcessedDate(LocalDateTime.now());
        seller.setRejectReason(reason);

        sellerRepository.save(seller);

        log.info("판매자 신청 거부 - 신청ID: {}, 사유: {}", sellerId, reason);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPendingApplication(String email) {
        Members member = membersRepository.findByEmail(email);
        if (member == null) {
            return false;
        }

        return sellerRepository.existsByMember_IdAndStatus(
                member.getId(), SellerStatus.PENDING
        );
    }
    
    @Override
    public void updateApplication(Long sellerId, SellerDTO dto) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("신청 내역을 찾을 수 없습니다."));
        
        // 승인 대기 중인 신청만 수정 가능
        if (seller.getStatus() != SellerStatus.PENDING) {
            throw new IllegalStateException("승인 대기 중인 신청만 수정할 수 있습니다.");
        }
        
        // 정보 업데이트
        seller.setBusinessNumber(dto.getBusinessNumber());
        seller.setBusinessName(dto.getBusinessName());
        seller.setRepresentativeName(dto.getRepresentativeName());
        seller.setBusinessAddress(dto.getBusinessAddress());
        seller.setPhone(dto.getPhone());
        seller.setCategory(dto.getCategory());
        seller.setIntroduction(dto.getIntroduction());
        
        sellerRepository.save(seller);
        
        log.info("판매자 신청 수정 완료 - 신청ID: {}", sellerId);
    }

    // Entity → DTO 변환
    private SellerDTO convertToDTO(Seller seller) {
        return SellerDTO.builder()
                .id(seller.getId())
                .memberId(seller.getMember().getId())
                .memberEmail(seller.getMember().getEmail())
                .memberName(seller.getMember().getName())
                .businessNumber(seller.getBusinessNumber())
                .businessName(seller.getBusinessName())
                .representativeName(seller.getRepresentativeName())
                .businessAddress(seller.getBusinessAddress())
                .phone(seller.getPhone())
                .category(seller.getCategory())
                .introduction(seller.getIntroduction())
                .status(seller.getStatus())
                .statusDescription(seller.getStatus().getDescription())
                .appliedDate(seller.getAppliedDate())
                .processedDate(seller.getProcessedDate())
                .rejectReason(seller.getRejectReason())
                .build();
    }
}