package com.example.airecommender.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class RecommendationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private LocalDateTime recommendedAt;

    public RecommendationHistory() {
    }

    public RecommendationHistory(User user, Product product, LocalDateTime recommendedAt) {
        this.user = user;
        this.product = product;
        this.recommendedAt = recommendedAt;
    }
}
