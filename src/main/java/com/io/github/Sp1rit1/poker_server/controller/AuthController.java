package com.io.github.Sp1rit1.poker_server.controller;

import com.io.github.Sp1rit1.poker_server.dto.AuthResponseDto;
import com.io.github.Sp1rit1.poker_server.dto.LoginRequestDto;
import com.io.github.Sp1rit1.poker_server.dto.UserRegistrationDto;
import com.io.github.Sp1rit1.poker_server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // @RestController, @RequestMapping и т.д.

@RestController // Помечаем как REST контроллер
@RequestMapping("/api/auth") // Базовый путь для всех эндпоинтов этого контроллера
@RequiredArgsConstructor // Lombok для внедрения UserService
public class AuthController {

    private final UserService userService; // Внедряем наш сервис

    @PostMapping("/register") // Обрабатывает POST запросы на /api/auth/register
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        // @Valid - включает валидацию DTO
        // @RequestBody - берет данные из тела запроса (JSON)
        try {
            userService.registerUser(registrationDto);
            // При успехе возвращаем статус 201 Created
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        } catch (RuntimeException e) {
            // Если возникла ошибка (например, пользователь уже существует)
            // Возвращаем статус 409 Conflict и сообщение об ошибке
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            // Примечание: Обработка ошибок здесь упрощенная. В реальных приложениях
            // часто используют @ControllerAdvice для централизованной обработки.
        }
    }

    @PostMapping("/login") // Обрабатывает POST запросы на /api/auth/login
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginDto) {
        try {
            AuthResponseDto authResponse = userService.authenticateUser(loginDto);
            // При успехе возвращаем статус 200 OK и DTO с userId и username
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            // Если возникла ошибка (неверный логин или пароль)
            // Возвращаем статус 401 Unauthorized и сообщение об ошибке
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}