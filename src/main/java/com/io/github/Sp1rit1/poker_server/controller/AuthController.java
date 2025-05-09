package com.io.github.Sp1rit1.poker_server.controller;

import com.io.github.Sp1rit1.poker_server.config.CustomUserDetails; // Импорт вашего CustomUserDetails
import com.io.github.Sp1rit1.poker_server.dto.AuthResponseDto;
import com.io.github.Sp1rit1.poker_server.dto.LoginRequestDto;
import com.io.github.Sp1rit1.poker_server.dto.UserRegistrationDto;
import com.io.github.Sp1rit1.poker_server.service.UserService; // UserService все еще нужен для регистрации
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService; // Для регистрации
    private final AuthenticationManager authenticationManager; // Внедряем AuthenticationManager

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            userService.registerUser(registrationDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        } catch (RuntimeException e) {
            // Логирование исключения на сервере рекомендуется
            // logger.error("Registration error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginDto, HttpServletRequest request) {
        try {
            // 1. Создаем токен аутентификации с учетными данными из запроса
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginDto.getUsername(),
                    loginDto.getPassword()
            );

            // 2. Выполняем аутентификацию через AuthenticationManager
            // Это вызовет ваш MyUserDetailsService для загрузки пользователя и PasswordEncoder для проверки пароля
            Authentication authentication = authenticationManager.authenticate(authToken);

            // 3. Если аутентификация прошла успешно, устанавливаем объект Authentication в SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 4. Явно сохраняем SecurityContext в HTTP сессию.
            // Spring Security обычно делает это автоматически через SecurityContextPersistenceFilter,
            // но явное сохранение здесь может помочь обеспечить консистентность или решить проблемы,
            // если стандартный механизм по какой-то причине не срабатывает как ожидается.
            HttpSession session = request.getSession(true); // true - создать сессию, если ее нет
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            // 5. Получаем детали пользователя из объекта Authentication для ответа
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            AuthResponseDto authResponse = new AuthResponseDto(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getFriendCode()
            );

            return ResponseEntity.ok(authResponse);

        } catch (AuthenticationException e) {
            // Это специфическое исключение для неудачной аутентификации
            // logger.warn("Authentication failed for user {}: {}", loginDto.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (RuntimeException e) {
            // Другие возможные RuntimeException
            // logger.error("Unexpected error during login for user {}: {}", loginDto.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during login.");
        }
    }
}