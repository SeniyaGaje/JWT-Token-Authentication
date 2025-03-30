package com.example.Token_Authentication.repository;

import com.example.Token_Authentication.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}