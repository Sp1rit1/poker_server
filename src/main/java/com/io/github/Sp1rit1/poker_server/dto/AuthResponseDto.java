package com.io.github.Sp1rit1.poker_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// @AllArgsConstructor // Возможно, придется обновить или добавить конструктор вручную
public class AuthResponseDto {
    private Long userId;
    private String username;
    private String friendCode;
    private String accessToken; // <-- НОВОЕ ПОЛЕ

    public AuthResponseDto(Long userId, String username, String friendCode, String accessToken) {
        this.userId = userId;
        this.username = username;
        this.friendCode = friendCode;
        this.accessToken = accessToken;
    }
}