package com.example.webshopping.repository;


import com.example.webshopping.entity.Cart;
import com.example.webshopping.entity.Members;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByMembers_Id(Long membersId);
    
    Cart findByMembers(Members members);
}
