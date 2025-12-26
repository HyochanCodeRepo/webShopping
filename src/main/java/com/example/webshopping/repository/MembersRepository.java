package com.example.webshopping.repository;

import com.example.webshopping.entity.Members;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembersRepository extends JpaRepository<Members, Long> {

    public Members findByEmail(String email);


}
