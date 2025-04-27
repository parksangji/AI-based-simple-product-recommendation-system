package com.example.airecommender.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserRequestDto {
    private String username;
    private String email;

    public UserRequestDto() {
    }

    public UserRequestDto(String username, String email) {
        this.username = username;
        this.email = email;
    }

}
