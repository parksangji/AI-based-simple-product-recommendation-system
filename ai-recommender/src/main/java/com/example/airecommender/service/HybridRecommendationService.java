package com.example.airecommender.service;

import com.example.airecommender.domain.Product;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HybridRecommendationService {

    private final UserBasedRecommendationService userBasedRecommendationService;
    private final ItemBasedRecommendationService itemBasedRecommendationService;
    private final ProductService productService;

    public HybridRecommendationService(UserBasedRecommendationService userBasedRecommendationService, ItemBasedRecommendationService itemBasedRecommendationService, ProductService productService) {
        this.userBasedRecommendationService = userBasedRecommendationService;
        this.itemBasedRecommendationService = itemBasedRecommendationService;
        this.productService = productService;
    }

    private final double USER_WEIGHT = 0.5;
    private final double ITEM_WEIGHT = 0.5;

    public List<Product> recommendHybrid(Long userId, int topN) {
        List<Product> userBasedRecommendations = userBasedRecommendationService.recommendProductsByUser(userId, topN * 2);
        List<Product> itemBasedRecommendations = itemBasedRecommendationService.recommendProductsByItem(userId, topN * 2);

        Map<Long, Double> combinedScores = new HashMap<>();
        
        for (int i = 0; i < userBasedRecommendations.size(); i++) {
            Product product = userBasedRecommendations.get(i);
            combinedScores.put(product.getId(), combinedScores.getOrDefault(product.getId(), 0.0) + (USER_WEIGHT * (topN * 2 - i)));
        }
        
        for (int i = 0; i < itemBasedRecommendations.size(); i++) {
            Product product = itemBasedRecommendations.get(i);
            combinedScores.put(product.getId(), combinedScores.getOrDefault(product.getId(), 0.0) + (ITEM_WEIGHT * (topN * 2 - i)));
        }
        
        return combinedScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> productService.getProductById(entry.getKey()))
                .collect(Collectors.toList());
    }
}
