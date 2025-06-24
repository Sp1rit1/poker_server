package com.io.github.Sp1rit1.poker_server.controller;

import com.io.github.Sp1rit1.poker_server.security.CustomUserDetails;
import com.io.github.Sp1rit1.poker_server.dto.UserStatsDto;
import com.io.github.Sp1rit1.poker_server.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/me") // Эндпоинт для получения статистики текущего аутентифицированного пользователя
    public ResponseEntity<?> getCurrentPlayerStats(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "User not authenticated properly."));
        }

        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUserDetails.getId();

        UserStatsDto statsDto = statsService.getPlayerStats(currentUserId);
        return ResponseEntity.ok(statsDto);
    }
}