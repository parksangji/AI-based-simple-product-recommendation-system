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

    public Map<String, Double> evaluateRecommendations(int topN) {
        List<User> allUsers = userRepository.findAll();
        int hitCount = 0;
        double mrrSum = 0;
        Set<Long> allRecommendedProducts = new HashSet<>();
        int totalRecommendations = 0;

        for (User user : allUsers) {
            List<ViewHistory> userHistory = viewHistoryRepository.findByUserOrderByViewedAtDesc(user);
            if (userHistory.size() < 2) continue;

            Product lastViewedProduct = userHistory.getFirst().getProduct();
            List<ViewHistory> trainingHistory = userHistory.subList(1, userHistory.size());

            Map<Long, Double> userSimilarities = calculateUserSimilaritiesForTraining(user.getId(), trainingHistory);
            List<Product> recommendations = generateRecommendationsFromSimilarUsers(user.getId(), trainingHistory, userSimilarities, topN);

            totalRecommendations += recommendations.size();
            allRecommendedProducts.addAll(recommendations.stream().map(Product::getId).collect(Collectors.toSet()));

            // 적중률 계산
            if (recommendations.stream().anyMatch(p -> p.getId().equals(lastViewedProduct.getId()))) {
                hitCount++;
                // MRR 계산
                for (int i = 0; i < recommendations.size(); i++) {
                    if (recommendations.get(i).getId().equals(lastViewedProduct.getId())) {
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

    private Map<Long, Double> calculateUserSimilaritiesForTraining(Long targetUserId, List<ViewHistory> trainingHistory) {
        Map<Long, Double> similarities = new HashMap<>();
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다. ID: " + targetUserId));

        List<User> allUsers = userRepository.findAll();
        for (User otherUser : allUsers) {
            if (!otherUser.getId().equals(targetUserId)) {
                List<ViewHistory> otherUserHistory = viewHistoryRepository.findByUserOrderByViewedAtDesc(otherUser);
                double similarity = calculateCosineSimilarityForTraining(trainingHistory, otherUserHistory);
                if (!Double.isNaN(similarity)) {
                    similarities.put(otherUser.getId(), similarity);
                }
            }
        }
        return similarities;
    }

    private double calculateCosineSimilarityForTraining(List<ViewHistory> history1, List<ViewHistory> history2) {
        Map<Long, Integer> productRatings1 = getViewHistoryMap(history1);
        Map<Long, Integer> productRatings2 = getViewHistoryMap(history2);

        double dotProduct = 0;
        double magnitude1 = 0;
        double magnitude2 = 0;

        for (Long productId : productRatings1.keySet()) {
            if (productRatings2.containsKey(productId)) {
                dotProduct += productRatings1.get(productId) * productRatings2.get(productId);
            }
            magnitude1 += Math.pow(productRatings1.get(productId), 2);
        }

        for (Integer rating : productRatings2.values()) {
            magnitude2 += Math.pow(rating, 2);
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }

    private Map<Long, Integer> getViewHistoryMap(List<ViewHistory> history) {
        Map<Long, Integer> productRatings = new HashMap<>();
        for (ViewHistory record : history) {
            productRatings.put(record.getProduct().getId(), record.getClickCount());
        }
        return productRatings;
    }

    private List<Product> generateRecommendationsFromSimilarUsers(Long targetUserId, List<ViewHistory> trainingHistory, Map<Long, Double> userSimilarities, int topN) {
        Set<Long> viewedProductIds = trainingHistory.stream().map(vh -> vh.getProduct().getId()).collect(Collectors.toSet());
        Map<Long, Double> recommendedProductScores = new HashMap<>();

        List<Map.Entry<Long, Double>> sortedSimilarities = userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .toList();

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
