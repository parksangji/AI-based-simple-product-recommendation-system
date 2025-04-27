package com.example.airecommender.service;

import com.example.airecommender.domain.Product;
import com.example.airecommender.domain.User;
import com.example.airecommender.domain.ViewHistory;
import com.example.airecommender.repository.ProductRepository;
import com.example.airecommender.repository.UserRepository;
import com.example.airecommender.repository.ViewHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserBasedRecommendationService {

    private final UserSimilarityService userSimilarityService;
    private final ViewHistoryRepository viewHistoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public UserBasedRecommendationService(UserSimilarityService userSimilarityService, ViewHistoryRepository viewHistoryRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.userSimilarityService = userSimilarityService;
        this.viewHistoryRepository = viewHistoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Product> recommendProductsByUser(Long userId, int topN) {
        Map<Long, Double> userSimilarities = userSimilarityService.calculateUserSimilarities(userId);

        if (userSimilarities.isEmpty()) {
            return productRepository.findAll().stream().limit(topN).collect(Collectors.toList());
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다. ID: " + userId));
        List<ViewHistory> targetUserHistory = viewHistoryRepository.findByUserOrderByViewedAtDesc(targetUser);
        Set<Long> viewedProductIds = targetUserHistory.stream().map(vh -> vh.getProduct().getId()).collect(Collectors.toSet());

        List<Map.Entry<Long, Double>> sortedSimilarities = userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .toList();

        Map<Long, Double> recommendedProductScores = new HashMap<>();

        for (Map.Entry<Long, Double> similarityEntry : sortedSimilarities) {
            Long similarUserId = similarityEntry.getKey();
            double similarityScore = similarityEntry.getValue();
            User similarUser = userRepository.findById(similarUserId)
                    .orElseThrow(() -> new RuntimeException("유사한 사용자를 찾을 수 없습니다. ID: " + similarUserId));
            List<ViewHistory> similarUserHistory = viewHistoryRepository.findByUserOrderByViewedAtDesc(similarUser);

            for (ViewHistory history : similarUserHistory) {
                Long productId = history.getProduct().getId();
                if (!viewedProductIds.contains(productId)) {
                    recommendedProductScores.put(productId, recommendedProductScores.getOrDefault(productId, 0.0) + similarityScore * history.getClickCount());
                }
            }
        }

        return recommendedProductScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> productRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
