package com.example.airecommender.service;

import com.example.airecommender.domain.Product;
import com.example.airecommender.domain.User;
import com.example.airecommender.domain.ViewHistory;
import com.example.airecommender.dto.ViewHistoryRequestDto;
import com.example.airecommender.repository.ProductRepository;
import com.example.airecommender.repository.UserRepository;
import com.example.airecommender.repository.ViewHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ViewHistoryService {

    private final ViewHistoryRepository viewHistoryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ViewHistoryService(ViewHistoryRepository viewHistoryRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.viewHistoryRepository = viewHistoryRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void recordViewHistory(ViewHistoryRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new NoSuchElementException("해당 사용자를 찾을 수 없습니다. ID: " + requestDto.getUserId()));
        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new NoSuchElementException("해당 상품을 찾을 수 없습니다. ID: " + requestDto.getProductId()));

        Optional<ViewHistory> existingHistory = viewHistoryRepository.findByUserAndProduct(user, product);

        if (existingHistory.isPresent()) {
            ViewHistory history = existingHistory.get();
            history.incrementClickCount();
            history.setViewedAt(LocalDateTime.now());
            viewHistoryRepository.save(history);
        } else {
            ViewHistory viewHistory = new ViewHistory(user, product, LocalDateTime.now());
            viewHistoryRepository.save(viewHistory);
        }
    }
}
