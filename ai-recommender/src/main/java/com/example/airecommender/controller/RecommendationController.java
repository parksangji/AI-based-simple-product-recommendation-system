package com.example.airecommender.controller;

import com.example.airecommender.domain.Product;
import com.example.airecommender.service.ItemBasedRecommendationService;
import com.example.airecommender.service.RecommendationService;
import com.example.airecommender.service.UserBasedRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserBasedRecommendationService userBasedRecommendationService;
    private final ItemBasedRecommendationService itemBasedRecommendationService;

    public RecommendationController(RecommendationService recommendationService, UserBasedRecommendationService userBasedRecommendationService, ItemBasedRecommendationService itemBasedRecommendationService) {
        this.recommendationService = recommendationService;
        this.userBasedRecommendationService = userBasedRecommendationService;
        this.itemBasedRecommendationService = itemBasedRecommendationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Product>> getRecommendations(@PathVariable Long userId) {
        List<Product> recommendations = recommendationService.recommendProductsAndRecord(userId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/user-based/{userId}")
    public ResponseEntity<List<Product>> getRecommendationsByUser(@PathVariable Long userId) {
        List<Product> recommendations = userBasedRecommendationService.recommendProductsByUser(userId, 5);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/user-based/evaluate")
    public ResponseEntity<Map<String, Double>> evaluateUserBasedRecommendations() {
        Map<String, Double> evaluationResults = userBasedRecommendationService.evaluateRecommendations(5);
        return ResponseEntity.ok(evaluationResults);
    }

    @GetMapping("/item-based/{userId}")
    public ResponseEntity<List<Product>> getRecommendationsByItem(@PathVariable Long userId) {
        List<Product> recommendations = itemBasedRecommendationService.recommendProductsByItem(userId, 5);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/item-based/evaluate")
    public ResponseEntity<Map<String, Double>> evaluateItemBasedRecommendations() {
        Map<String, Double> evaluationResults = itemBasedRecommendationService.evaluateItemBasedRecommendations(5);
        return ResponseEntity.ok(evaluationResults);
    }
}