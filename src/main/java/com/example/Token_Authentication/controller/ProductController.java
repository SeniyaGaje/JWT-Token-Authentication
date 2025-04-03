package com.example.Token_Authentication.controller;

import com.example.Token_Authentication.entity.ProductEntity;
import com.example.Token_Authentication.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        try {
            return ResponseEntity.ok(productService.getAllProducts());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch products", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_OWNER')")
    public ResponseEntity<?> createProduct(@RequestBody ProductEntity product) {
        try {
            return ResponseEntity.ok(productService.createProduct(product));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Product creation failed", e.getMessage()));
        }
    }

    @Getter
    @AllArgsConstructor
    private static class ErrorResponse {
        private final String error;
        private final String details;
    }
}