package com.example.airecommender.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ViewHistoryRequestDto {
    private Long userId;
    private Long productId;

    public ViewHistoryRequestDto() {
    }

    public ViewHistoryRequestDto(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }
}