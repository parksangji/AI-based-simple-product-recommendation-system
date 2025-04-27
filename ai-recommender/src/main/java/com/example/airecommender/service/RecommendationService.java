package com.example.airecommender.service;

import com.example.airecommender.domain.*;
import com.example.airecommender.repository.ProductRepository;
import com.example.airecommender.repository.RecommendationHistoryRepository;
import com.example.airecommender.repository.UserRepository;
import com.example.airecommender.repository.ViewHistoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RecommendationHistoryRepository recommendationHistoryRepository;
    private final ViewHistoryRepository viewHistoryRepository;

    public RecommendationService(ProductRepository productRepository, UserRepository userRepository, RecommendationHistoryRepository recommendationHistoryRepository, ViewHistoryRepository viewHistoryRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.recommendationHistoryRepository = recommendationHistoryRepository;
        this.viewHistoryRepository = viewHistoryRepository;
    }

    @Transactional
    public List<Product> recommendProductsAndRecord(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 사용자를 찾을 수 없습니다. ID: " + userId));
        List<Product> recommendations = recommendProductsByViewHistory(user);
        recordRecommendationHistory(user, recommendations);
        return recommendations;
    }

    // 규칙 기반 추천 로직: 최근 조회 상품과 같은 카테고리의 다른 상품 추천
    private List<Product> recommendProductsByViewHistory(User user) {
        List<ViewHistory> recentViews = viewHistoryRepository.findByUserOrderByViewedAtDesc(user);

        if (recentViews.isEmpty()) {
            return productRepository.findAll().stream().limit(3).collect(Collectors.toList());
        }

        Product recentlyViewedProduct = recentViews.getFirst().getProduct();
        Category category = recentlyViewedProduct.getCategory();

        return productRepository.findByCategory(category).stream()
                .filter(product -> !product.equals(recentlyViewedProduct))
                .limit(3)
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
