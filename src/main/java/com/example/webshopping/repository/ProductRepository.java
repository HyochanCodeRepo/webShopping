package com.example.webshopping.repository;

import com.example.webshopping.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    public List<Product> findByCategory_Id(Long categoryId);

    List<Product> findByMembers_IdOrderByCreatedDateDesc(Long memberId);

}
