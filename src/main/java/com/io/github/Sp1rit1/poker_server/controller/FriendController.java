package com.io.github.Sp1rit1.poker_server.controller;

import com.io.github.Sp1rit1.poker_server.dto.FriendCodeRequestDto;
import com.io.github.Sp1rit1.poker_server.config.CustomUserDetails; // Убедитесь, что импорт правильный
import com.io.github.Sp1rit1.poker_server.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/add")
    public ResponseEntity<?> addFriend(@Valid @RequestBody FriendCodeRequestDto requestDto, Authentication authentication) {
        // Более строгая проверка объекта Authentication и типа Principal
        if (authentication == null || !authentication.isAuthenticated()) {
            // Если объект Authentication отсутствует или пользователь не аутентифицирован
            // (хотя правило .authenticated() в SecurityConfig должно это покрывать)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            // Если Principal не является ожидаемым CustomUserDetails
            // Это может указывать на проблему в конфигурации безопасности или UserDetailsService
            // Логирование этой ситуации на сервере было бы полезно для отладки
            // logger.error("Unexpected Principal type: {}", principal.getClass().getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated properly or unexpected Principal type.");
        }

        // Теперь мы уверены, что principal - это CustomUserDetails
        CustomUserDetails currentUserDetails = (CustomUserDetails) principal;
        Long currentUserId = currentUserDetails.getId();

        try {
            friendService.addFriend(currentUserId, requestDto.getFriendCode());
            return ResponseEntity.ok("Friend added successfully.");
        } catch (RuntimeException e) {
            // Логирование исключения на сервере также рекомендуется перед возвратом ответа
            // logger.error("Error adding friend for user {}: {}", currentUserId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
