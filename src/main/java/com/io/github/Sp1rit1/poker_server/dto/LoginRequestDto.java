package com.io.github.Sp1rit1.poker_server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto { // класс, объекты которого будут использоваться для передачи данных о входе между слоями

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}