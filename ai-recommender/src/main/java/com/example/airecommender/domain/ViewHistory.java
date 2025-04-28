package com.example.airecommender.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class ViewHistory {
    // Getter 및 Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private LocalDateTime viewedAt;

    private int clickCount;

    public ViewHistory() {
        this.clickCount = 0;
    }

    public ViewHistory(User user, Product product, LocalDateTime viewedAt) {
        this.user = user;
        this.product = product;
        this.viewedAt = viewedAt;
        this.clickCount = 1;
    }
    public void incrementClickCount() {
        this.clickCount++;
    }
}