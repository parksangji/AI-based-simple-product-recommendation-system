package com.example.airecommender.service;

import com.example.airecommender.domain.ViewHistory;
import com.example.airecommender.repository.ViewHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSimilarityService {

    private final ViewHistoryRepository viewHistoryRepository;

    public ItemSimilarityService(ViewHistoryRepository viewHistoryRepository) {
        this.viewHistoryRepository = viewHistoryRepository;
    }

    public Map<Long, Map<Long, Double>> calculateItemSimilarities() {
        Map<Long, Map<Long, Double>> itemSimilarities = new HashMap<>();
        List<ViewHistory> allViewHistories = viewHistoryRepository.findAll();

        Map<Long, Map<Long, Integer>> productUserRatings = new HashMap<>();
        for (ViewHistory history : allViewHistories) {
            Long productId = history.getProduct().getId();
            Long userId = history.getUser().getId();
            int clickCount = history.getClickCount();
            productUserRatings.computeIfAbsent(productId, k -> new HashMap<>()).put(userId, clickCount);
        }

        List<Long> productIds = new ArrayList<>(productUserRatings.keySet());

        for (int i = 0; i < productIds.size(); i++) {
            Long productId1 = productIds.get(i);
            itemSimilarities.computeIfAbsent(productId1, k -> new HashMap<>());
            for (int j = i + 1; j < productIds.size(); j++) {
                Long productId2 = productIds.get(j);
                double similarity = calculateCosineSimilarity(
                        productUserRatings.getOrDefault(productId1, new HashMap<>()),
                        productUserRatings.getOrDefault(productId2, new HashMap<>())
                );
                if (!Double.isNaN(similarity)) {
                    itemSimilarities.get(productId1).put(productId2, similarity);
                    itemSimilarities.computeIfAbsent(productId2, k -> new HashMap<>()).put(productId1, similarity);
                }
            }
        }

        return itemSimilarities;
    }

    private double calculateCosineSimilarity(Map<Long, Integer> ratings1, Map<Long, Integer> ratings2) {
        double dotProduct = 0;
        double magnitude1 = 0;
        double magnitude2 = 0;

        for (Long userId : ratings1.keySet()) {
            if (ratings2.containsKey(userId)) {
                dotProduct += ratings1.get(userId) * ratings2.get(userId);
            }
            magnitude1 += Math.pow(ratings1.get(userId), 2);
        }

        for (Integer rating : ratings2.values()) {
            magnitude2 += Math.pow(rating, 2);
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }
}