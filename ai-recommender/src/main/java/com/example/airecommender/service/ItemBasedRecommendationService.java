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
public class ItemBasedRecommendationService {

    private final ItemSimilarityService itemSimilarityService;
    private final ViewHistoryRepository viewHistoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ItemBasedRecommendationService(ItemSimilarityService itemSimilarityService, ViewHistoryRepository viewHistoryRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.itemSimilarityService = itemSimilarityService;
        this.viewHistoryRepository = viewHistoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Product> recommendProductsByItem(Long userId, int topN) {
        Map<Long, Map<Long, Double>> itemSimilarities = itemSimilarityService.calculateItemSimilarities();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다. ID: " + userId));
        List<ViewHistory> userViewHistory = viewHistoryRepository.findByUserOrderByViewedAtDesc(user);
        Set<Long> viewedProductIds = userViewHistory.stream().map(vh -> vh.getProduct().getId()).collect(Collectors.toSet());
        Map<Long, Double> recommendationScores = new HashMap<>();

        for (ViewHistory history : userViewHistory) {
            Long viewedProductId = history.getProduct().getId();
            int clickCount = history.getClickCount();
            Map<Long, Double> similarItems = itemSimilarities.getOrDefault(viewedProductId, new HashMap<>());

            for (Map.Entry<Long, Double> similarItemEntry : similarItems.entrySet()) {
                Long similarItemId = similarItemEntry.getKey();
                double similarityScore = similarItemEntry.getValue();

                if (!viewedProductIds.contains(similarItemId)) {
                    recommendationScores.put(similarItemId, recommendationScores.getOrDefault(similarItemId, 0.0) + similarityScore * clickCount);
                }
            }
        }

        return recommendationScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> productRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Map<String, Double> evaluateItemBasedRecommendations(int topN) {
        Map<Long, Map<Long, Double>> itemSimilarities = itemSimilarityService.calculateItemSimilarities();
        List<User> allUsers = userRepository.findAll();
        int hitCount = 0;
        double mrrSum = 0;
        Set<Long> allRecommendedProducts = new HashSet<>();
        int totalRecommendations = 0;

        for (User user : allUsers) {
            List<ViewHistory> userHistory = viewHistoryRepository.findByUserOrderByViewedAtDesc(user);
            if (userHistory.size() < 2) continue;

            Product lastViewedProduct = userHistory.get(0).getProduct();
            List<ViewHistory> trainingHistory = userHistory.subList(1, userHistory.size());
            Set<Long> trainingViewedProductIds = trainingHistory.stream().map(vh -> vh.getProduct().getId()).collect(Collectors.toSet());

            Map<Long, Double> recommendationScores = new HashMap<>();
            for (ViewHistory history : trainingHistory) {
                Long viewedProductId = history.getProduct().getId();
                int clickCount = history.getClickCount();
                Map<Long, Double> similarItems = itemSimilarities.getOrDefault(viewedProductId, new HashMap<>());

                for (Map.Entry<Long, Double> similarItemEntry : similarItems.entrySet()) {
                    Long similarItemId = similarItemEntry.getKey();
                    double similarityScore = similarItemEntry.getValue();

                    if (!trainingViewedProductIds.contains(similarItemId)) {
                        recommendationScores.put(similarItemId, recommendationScores.getOrDefault(similarItemId, 0.0) + similarityScore * clickCount);
                    }
                }
            }

            List<Long> recommendations = recommendationScores.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(topN)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            totalRecommendations += recommendations.size();
            allRecommendedProducts.addAll(recommendations);

            if (recommendations.contains(lastViewedProduct.getId())) {
                hitCount++;
                for (int i = 0; i < recommendations.size(); i++) {
                    if (recommendations.get(i).equals(lastViewedProduct.getId())) {
                        mrrSum += 1.0 / (i + 1);
                        break;
                    }
                }
            }
        }

        double hitRate = (double) hitCount / (allUsers.size() > 0 ? allUsers.size() : 1);
        double mrr = mrrSum / (allUsers.size() > 0 ? allUsers.size() : 1);
        double diversity = totalRecommendations > 0 ? (double) allRecommendedProducts.size() / totalRecommendations : 0;

        Map<String, Double> evaluationResults = new HashMap<>();
        evaluationResults.put("hitRate", hitRate);
        evaluationResults.put("mrr", mrr);
        evaluationResults.put("diversity", diversity);

        return evaluationResults;
    }
}