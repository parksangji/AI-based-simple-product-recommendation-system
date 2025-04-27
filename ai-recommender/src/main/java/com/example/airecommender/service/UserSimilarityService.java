package com.example.airecommender.service;

import com.example.airecommender.domain.User;
import com.example.airecommender.domain.ViewHistory;
import com.example.airecommender.repository.UserRepository;
import com.example.airecommender.repository.ViewHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserSimilarityService {

    private final ViewHistoryRepository viewHistoryRepository;
    private final UserRepository userRepository;

    public UserSimilarityService(ViewHistoryRepository viewHistoryRepository, UserRepository userRepository) {
        this.viewHistoryRepository = viewHistoryRepository;
        this.userRepository = userRepository;
    }

    public Map<Long, Double> calculateUserSimilarities(Long targetUserId) {
        Map<Long, Double> similarities = new HashMap<>();
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다. ID: " + targetUserId));
        List<ViewHistory> targetUserHistory = viewHistoryRepository.findByUserOrderByViewedAtDesc(targetUser);

        if (targetUserHistory.isEmpty()) {
            return similarities;
        }

        List<User> allUsers = userRepository.findAll();
        for (User otherUser : allUsers) {
            if (!otherUser.getId().equals(targetUserId)) {
                List<ViewHistory> otherUserHistory = viewHistoryRepository.findByUserOrderByViewedAtDesc(otherUser);
                double similarity = calculateCosineSimilarity(targetUserHistory, otherUserHistory);
                if (!Double.isNaN(similarity)) {
                    similarities.put(otherUser.getId(), similarity);
                }
            }
        }
        return similarities;
    }

    private double calculateCosineSimilarity(List<ViewHistory> history1, List<ViewHistory> history2) {
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
}
