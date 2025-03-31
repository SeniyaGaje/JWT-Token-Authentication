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
        try {
            return productRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch products: " + e.getMessage());
        }
    }

    public ProductEntity createProduct(ProductEntity product) {
        try {
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                throw new RuntimeException("Product name cannot be empty");
            }
            if (product.getPrice() <= 0) {
                throw new RuntimeException("Product price must be positive");
            }
            return productRepository.save(product);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create product: " + e.getMessage());
        }
    }
}