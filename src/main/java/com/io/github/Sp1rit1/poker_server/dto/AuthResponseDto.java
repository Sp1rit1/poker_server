package com.io.github.Sp1rit1.poker_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Добавим и пустой конструктор на всякий случай
@AllArgsConstructor // Генерирует конструктор userId, username
public class AuthResponseDto {
    private Long userId;
    private String username;
    // private String accessToken; // Для токена в будущем
}