package com.io.github.Sp1rit1.poker_server.controller;

import com.io.github.Sp1rit1.poker_server.security.CustomUserDetails; // Убедитесь, что этот импорт корректен
import com.io.github.Sp1rit1.poker_server.dto.AuthResponseDto;
import com.io.github.Sp1rit1.poker_server.dto.LoginRequestDto;
import com.io.github.Sp1rit1.poker_server.dto.UserRegistrationDto;
import com.io.github.Sp1rit1.poker_server.entity.User;
import com.io.github.Sp1rit1.poker_server.security.jwt.JwtTokenProvider; // <-- НОВЫЙ ИМПОРТ
import com.io.github.Sp1rit1.poker_server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider; // <-- ВНЕДРЯЕМ JwtTokenProvider

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        User registeredUser = userService.registerUser(registrationDto);
        return ResponseEntity.status(201).body(java.util.Map.of("message", "User registered successfully! Please log in."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        // 1. Создаем Authentication токен из запроса для AuthenticationManager
        UsernamePasswordAuthenticationToken authTokenForManager = new UsernamePasswordAuthenticationToken(
                loginRequestDto.getUsername(),
                loginRequestDto.getPassword()
        );

        // 2. Аутентифицируем пользователя через AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(authTokenForManager);



        // 4. Генерируем JWT токен на основе успешной аутентификации
        String jwt = tokenProvider.generateToken(authentication);

        // 5. Получаем детали пользователя для ответа
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 6. Возвращаем токен и информацию о пользователе клиенту
        return ResponseEntity.ok(new AuthResponseDto(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getFriendCode(),
                jwt // <-- ПЕРЕДАЕМ JWT В DTO
        ));
    }
}