package com.example.airecommender.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductRequestDto {
    // Getter 및 Setter
    private String name;
    private String description;
    private double price;

    public ProductRequestDto() {
    }

    public ProductRequestDto(String name, String description, double price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }
}