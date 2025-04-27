package com.example.airecommender.controller;

import com.example.airecommender.domain.Product;
import com.example.airecommender.dto.ProductRequestDto;
import com.example.airecommender.service.ProductService;
import com.example.airecommender.service.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final RecommendationService recommendationService;

    public ProductController(ProductService productService, RecommendationService recommendationService) {
        this.productService = productService;
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequestDto requestDto) {
        Product createdProduct = productService.createProduct(requestDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductRequestDto requestDto) {
        Product updatedProduct = productService.updateProduct(id, requestDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<List<Product>> getRecommendations(@PathVariable Long userId) {
        List<Product> recommendations = recommendationService.recommendProductsAndRecord(userId);
        return ResponseEntity.ok(recommendations);
    }
}