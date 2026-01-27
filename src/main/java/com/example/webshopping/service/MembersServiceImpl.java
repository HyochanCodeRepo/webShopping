package com.example.webshopping.service;

import com.example.webshopping.constant.Role;
import com.example.webshopping.dto.MembersDTO;
import com.example.webshopping.entity.Members;
import com.example.webshopping.repository.MembersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class MembersServiceImpl implements MembersService {
    private final MembersRepository membersRepository;
    private final ModelMapper modelMapper = new ModelMapper();
    private final PasswordEncoder passwordEncoder;




    @Override
    public void create(MembersDTO memberDTO) {
        Members members = modelMapper.map(memberDTO, Members.class);

        members.setPassword(passwordEncoder.encode(members.getPassword()));
        
        // DTO에 role이 설정되어 있지 않으면 기본값 USER 설정
        if (members.getRole() == null) {
            members.setRole(Role.ROLE_USER);
        }

        membersRepository.save(members);

    }
}
