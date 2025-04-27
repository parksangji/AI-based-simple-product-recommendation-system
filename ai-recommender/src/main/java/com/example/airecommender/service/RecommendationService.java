package com.example.airecommender.service;

import com.example.airecommender.domain.Product;
import com.example.airecommender.domain.User;
import com.example.airecommender.repository.ProductRepository;
import com.example.airecommender.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public RecommendationService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Product> recommendProducts(Long userId) {
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
}
