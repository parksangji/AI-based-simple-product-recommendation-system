package com.example.airecommender.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI 기반 상품 추천 시스템 API")
                        .version("v1")
                        .description("AI 기반의 간단한 상품 추천 시스템 API입니다."));
    }
}
