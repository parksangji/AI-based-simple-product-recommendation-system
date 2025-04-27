package com.example.airecommender.dto;

import lombok.Data;

@Data
public class ProductRequestDto {
    private String name;
    private String description;
    private double price;
}
