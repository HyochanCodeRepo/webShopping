package com.example.webshopping.entity;

import com.example.webshopping.constant.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Members {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String phone;
    private String address;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
}
