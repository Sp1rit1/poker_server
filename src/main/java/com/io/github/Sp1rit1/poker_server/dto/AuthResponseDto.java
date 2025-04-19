package com.io.github.Sp1rit1.poker_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto { // класс, объекты которого будут использоваться для передачи данных об аунтефикации между слоями
    private Long userId;
    private String username;
}