package com.example.Token_Authentication.service;

import com.example.Token_Authentication.entity.ProductEntity;
import com.example.Token_Authentication.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<ProductEntity> getAllProducts() {
        return productRepository.findAll();
    }

    public ProductEntity createProduct(ProductEntity product) {
        return productRepository.save(product);
    }
}