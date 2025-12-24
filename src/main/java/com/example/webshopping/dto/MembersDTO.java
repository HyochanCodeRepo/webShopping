package com.example.webshopping.dto;

import com.example.webshopping.constant.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MembersDTO {

    private Long id;

    private String name;
    private String email;
    private String phone;
    private String address;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

}
