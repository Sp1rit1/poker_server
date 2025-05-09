package com.io.github.Sp1rit1.poker_server.controller;

import com.io.github.Sp1rit1.poker_server.dto.FriendCodeRequestDto;
import com.io.github.Sp1rit1.poker_server.config.CustomUserDetails;
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
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated properly or unexpected Principal type.");
        }

        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUserDetails.getId();

        // try-catch для RuntimeException теперь не нужен, его обработает GlobalExceptionHandler
        friendService.addFriend(currentUserId, requestDto.getFriendCode());
        return ResponseEntity.ok("Friend added successfully.");
        // Если friendService.addFriend бросит RuntimeException (например, "User not found"),
        // GlobalExceptionHandler его перехватит и вернет 400 Bad Request с сообщением.
    }
}