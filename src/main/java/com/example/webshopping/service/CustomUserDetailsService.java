package com.example.webshopping.service;

import com.example.webshopping.entity.Members;
import com.example.webshopping.repository.MembersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {

    private final MembersRepository membersRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("spring security 로그인 : " + email);

        Members members =
            membersRepository.findByEmail(email);

        if (members == null) {
            log.error("사용자를 찾을 수 없습니다." +  email);
            throw new UsernameNotFoundException(email);
        }
        log.info("회원 조회 성공" + members.getEmail());


        //권한 설정
        List<GrantedAuthority>authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority((members.getRole().name())));



        return User.builder()
                .username(members.getEmail())
                .password(members.getPassword())
                .authorities(authorities)
                .build();
    }
}
