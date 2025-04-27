package com.example.airecommender.controller;

import com.example.airecommender.domain.Product;
import com.example.airecommender.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Product>> getRecommendations(@PathVariable Long userId) {
        List<Product> recommendations = recommendationService.recommendProductsAndRecord(userId);
        return ResponseEntity.ok(recommendations);
    }
}
