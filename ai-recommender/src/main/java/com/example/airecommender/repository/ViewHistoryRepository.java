package com.example.airecommender.repository;

import com.example.airecommender.domain.Product;
import com.example.airecommender.domain.User;
import com.example.airecommender.domain.ViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {
    List<ViewHistory> findByUserOrderByViewedAtDesc(User user);
    boolean existsByUserAndProduct(User user, Product product);

    Optional<ViewHistory> findByUserAndProduct(User user, Product product);
}
