package com.example.webshopping.controller;

import com.example.webshopping.constant.OrderStatus;
import com.example.webshopping.constant.Role;
import com.example.webshopping.dto.MembersDTO;
import com.example.webshopping.dto.OrderResponseDTO;
import com.example.webshopping.entity.Members;
import com.example.webshopping.repository.MembersRepository;
import com.example.webshopping.service.MembersService;
import com.example.webshopping.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/members")
public class MembersController {
    private final MembersService membersService;
    private final AuthenticationManager authenticationManager;
    private final MembersRepository membersRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderService orderService;

    @GetMapping("/register")
    public String register() {
        return "members/register";
    }

    @PostMapping("/register")
    public String register(MembersDTO memberDTO) {

        log.info(memberDTO);

        membersService.create(memberDTO);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "members/login";
    }

    /**
     * 시연용 계정 원클릭 로그인
     */
    @PostMapping("/demo-login")
    public String demoLogin(@RequestParam String role,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        String email = "";
        String password = "test1234";
        String roleName = "";

        switch(role) {
            case "admin":
                email = "admin@test.com";
                roleName = "관리자";
                break;
            case "seller":
                email = "seller@test.com";
                roleName = "판매자";
                break;
            case "user":
                email = "user@test.com";
                roleName = "구매자";
                break;
            default:
                redirectAttributes.addFlashAttribute("error", "잘못된 요청입니다.");
                return "redirect:/members/login";
        }

        try {
            // 계정이 없으면 생성
            Members member = membersRepository.findByEmail(email);
            if (member == null) {
                MembersDTO newMember = new MembersDTO();
                newMember.setEmail(email);
                newMember.setPassword(password);
                newMember.setName(roleName);
                newMember.setRole(Role.valueOf("ROLE_" + role.toUpperCase()));
                membersService.create(newMember);
                log.info("시연용 계정 생성: {} ({})", email, roleName);
            }

            // Spring Security 인증 처리
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, password);
            Authentication auth = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 세션에 인증 정보 저장
            request.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            log.info("시연용 로그인 성공: {} ({})", email, roleName);
            redirectAttributes.addFlashAttribute("message", roleName + " 계정으로 로그인되었습니다.");
            return "redirect:/";

        } catch (Exception e) {
            log.error("시연용 로그인 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "로그인에 실패했습니다.");
            return "redirect:/members/login";
        }
    }

    /**
     * 마이페이지
     */
    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }

        String email = userDetails.getUsername();
        Members member = membersRepository.findByEmail(email);
        
        if (member == null) {
            return "redirect:/members/login";
        }

        // 내 주문 전체 조회
        List<OrderResponseDTO> allOrders = orderService.getMyOrders(email);
        
        // 최근 주문 3개
        List<OrderResponseDTO> recentOrders = allOrders.stream()
                .limit(3)
                .collect(Collectors.toList());
        
        // 상태별 카운트
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("SHIPPED", allOrders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.SHIPPED)
                .count());
        statusCounts.put("DELIVERED", allOrders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED)
                .count());
        statusCounts.put("PREPARING", allOrders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.PREPARING)
                .count());
        
        // 전체 주문 수
        long totalOrders = allOrders.size();

        model.addAttribute("member", member);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("allOrders", allOrders);
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("totalOrders", totalOrders);
        
        return "members/mypage";
    }

    /**
     * 기본 정보 수정
     */
    @PostMapping("/mypage/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String name,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }

        try {
            String email = userDetails.getUsername();
            Members member = membersRepository.findByEmail(email);
            
            if (member == null) {
                throw new EntityNotFoundException("회원을 찾을 수 없습니다.");
            }

            member.setName(name);
            member.setPhone(phone);
            member.setAddress(address);
            
            membersRepository.save(member);

            log.info("회원 정보 수정 완료: {}", email);
            redirectAttributes.addFlashAttribute("message", "회원 정보가 수정되었습니다.");
            
        } catch (Exception e) {
            log.error("회원 정보 수정 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "회원 정보 수정에 실패했습니다.");
        }

        return "redirect:/members/mypage";
    }

    /**
     * 비밀번호 변경
     */
    @PostMapping("/mypage/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String newPasswordConfirm,
                                  RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/members/login";
        }

        try {
            String email = userDetails.getUsername();
            Members member = membersRepository.findByEmail(email);
            
            if (member == null) {
                throw new EntityNotFoundException("회원을 찾을 수 없습니다.");
            }

            // 현재 비밀번호 확인
            if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
                return "redirect:/members/mypage";
            }

            // 새 비밀번호 확인
            if (!newPassword.equals(newPasswordConfirm)) {
                redirectAttributes.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다.");
                return "redirect:/members/mypage";
            }

            // 비밀번호 길이 확인
            if (newPassword.length() < 4) {
                redirectAttributes.addFlashAttribute("error", "비밀번호는 4자 이상이어야 합니다.");
                return "redirect:/members/mypage";
            }

            // 비밀번호 변경
            member.setPassword(passwordEncoder.encode(newPassword));
            membersRepository.save(member);

            log.info("비밀번호 변경 완료: {}", email);
            redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
            
        } catch (Exception e) {
            log.error("비밀번호 변경 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "비밀번호 변경에 실패했습니다.");
        }

        return "redirect:/members/mypage";
    }

}
