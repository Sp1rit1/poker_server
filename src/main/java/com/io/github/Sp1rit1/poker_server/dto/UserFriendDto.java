package com.io.github.Sp1rit1.poker_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFriendDto {
    private Long userId;       // ID друга
    private String username;   // Имя пользователя друга
    private String friendCode; // Код дружбы друга (на случай, если понадобится)
    // Вы можете добавить сюда другие поля, если они нужны клиенту, например, онлайн-статус (но это сложнее)
}