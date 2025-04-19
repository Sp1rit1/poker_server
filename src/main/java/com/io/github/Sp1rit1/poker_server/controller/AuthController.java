package com.io.github.Sp1rit1.poker_server.controller;

import com.io.github.Sp1rit1.poker_server.dto.AuthResponseDto;
import com.io.github.Sp1rit1.poker_server.dto.LoginRequestDto;
import com.io.github.Sp1rit1.poker_server.dto.UserRegistrationDto;
import com.io.github.Sp1rit1.poker_server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // помечает бин, как контроллер, возвращаемые значения методов которого будут сериализованы в тело ответа (json) (@Controller + @ResponseBody)
@RequestMapping("/api/auth") // задаёт базовый url для всех методов
@RequiredArgsConstructor // генерирует конструктор с аргументами, являющимися неинициализированными final полями
public class AuthController { // контроллер для регистрации и входа

    private final UserService userService; // будет принят аргументом для конструктора AuthController, что позволит спрингу внедрить зависимость

    @PostMapping("/register") // данный метод становится эндпоинтом с соответствующим HTTP-методом и URL
    // эндпоинт с URL: POST /api/auth/register, ? - wildcard, т.к. тип ответа может быть разным, @RequestBody десериализирует тело HTTP-зароса из json в объект UserRegistrationDto
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            userService.registerUser(registrationDto); // регистрируем пользователя
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!"); // возвращаем HTTP-ответ с соответствующим статусом (201 Created) и телом ответа
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // возвращаем HTTP-ответ с соответствующим статусом (статус ошибки) и телом ответа
        }
    }

    @PostMapping("/login")
    // эндпоинт с URL: POST /api/auth/login, @RequestBody десериализирует тело HTTP-запроса из json в объект LoginRequestDto
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginDto) {
        try {
            AuthResponseDto authResponse = userService.authenticateUser(loginDto); // аутентифицируем пользователя
            return ResponseEntity.ok(authResponse); // возвращаем HTTP-ответ со статусом 200 OK и сериализованный в json объект authResponse в теле ответа
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage()); // возвращаем HTTP-ответ с соответствующим статусом (статус ошибки) и телом ответа
        }
    }
}