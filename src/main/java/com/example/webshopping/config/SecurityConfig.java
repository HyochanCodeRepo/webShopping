package com.example.webshopping.config;

import jakarta.servlet.annotation.WebListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                        authorize -> authorize
                                // ========== 공개 경로 (누구나 접근 가능) ==========
                                .requestMatchers("/", "/members/login", "/members/register", "/members/new").permitAll()
                                .requestMatchers("/css/**", "/js/**", "/images/**", "/img/**").permitAll()
                                .requestMatchers("/products", "/products/**", "/product/detail/**").permitAll()
                                
                                // ========== SELLER, ADMIN 공통 경로 (상품 관리) ==========
                                .requestMatchers("/admin/product/**").hasAnyRole("SELLER", "ADMIN")
                                .requestMatchers("/admin/orders/**").hasAnyRole("SELLER", "ADMIN")
                                
                                // ========== ADMIN, SELLER 메인 페이지 ==========
                                .requestMatchers("/admin").hasAnyRole("SELLER", "ADMIN")
                                
                                // ========== ADMIN 전용 경로 ==========
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                
                                // ========== 상품 등록/수정/삭제 ==========
                                .requestMatchers("/product/register", "/product/new").hasAnyRole("SELLER", "ADMIN")
                                .requestMatchers("/product/edit/**", "/product/update/**").hasAnyRole("SELLER", "ADMIN")
                                .requestMatchers("/product/delete/**").hasAnyRole("SELLER", "ADMIN")
                                
                                // ========== 판매자 신청 경로 (USER만 신청 가능) ==========
                                .requestMatchers("/seller/apply").hasRole("USER")
                                .requestMatchers("/seller/edit/**").hasRole("USER")
                                .requestMatchers("/seller/status").authenticated()
                                
                                // ========== 주문 관련 경로 (로그인 필요) ==========
                                .requestMatchers("/cart/**").authenticated()
                                .requestMatchers("/orders/**").authenticated()
                                .requestMatchers("/checkout/**").authenticated()
                                
                                // ========== 마이페이지 (로그인 필요) ==========
                                .requestMatchers("/mypage/**").authenticated()
                                
                                // ========== 그 외 모든 요청 (로그인 필요) ==========
                                .anyRequest().authenticated()
                ).csrf((csrf) -> csrf.disable())

                .formLogin(formLogin -> formLogin
                        .loginPage("/members/login")
                        .defaultSuccessUrl("/", true)
                        .usernameParameter("email")
                )
                .logout((logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                ))
                
                // ========== 접근 거부 처리 ==========
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                );
        return http.build();
    }

}
