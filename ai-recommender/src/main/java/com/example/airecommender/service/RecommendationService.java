package com.example.airecommender.service;

import com.example.airecommender.domain.Product;
import com.example.airecommender.domain.RecommendationHistory;
import com.example.airecommender.domain.User;
import com.example.airecommender.repository.ProductRepository;
import com.example.airecommender.repository.RecommendationHistoryRepository;
import com.example.airecommender.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RecommendationHistoryRepository recommendationHistoryRepository;
    private final Random random = new Random();


    public RecommendationService(ProductRepository productRepository, UserRepository userRepository, RecommendationHistoryRepository recommendationHistoryRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.recommendationHistoryRepository = recommendationHistoryRepository;
    }

    @Transactional
    public List<Product> recommendProductsAndRecord(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다. ID: " + userId));
        List<Product> recommendations = recommendProducts(userId);
        recordRecommendationHistory(user, recommendations);
        return recommendations;
    }

    // 간단한 랜덤 추천 로직 (기존 메소드)
    private List<Product> recommendProducts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다. ID: " + userId));
        List<Product> allProducts = productRepository.findAll();

        if (allProducts.isEmpty()) {
            return List.of();
        }

        return random.ints(0, allProducts.size())
                .distinct()
                .limit(3)
                .mapToObj(allProducts::get)
                .collect(Collectors.toList());
    }

    @Transactional
    public void recordRecommendationHistory(User user, List<Product> recommendedProducts) {
        LocalDateTime now = LocalDateTime.now();
        for (Product product : recommendedProducts) {
            RecommendationHistory history = new RecommendationHistory(user, product, now);
            recommendationHistoryRepository.save(history);
        }
    }
}
