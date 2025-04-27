package com.example.airecommender.repository;

import com.example.airecommender.domain.RecommendationHistory;
import com.example.airecommender.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {
    List<RecommendationHistory> findByUser(User user);
    List<RecommendationHistory> findByUserId(Long userId);
}
