package com.io.github.Sp1rit1.poker_server.controller;

import com.io.github.Sp1rit1.poker_server.security.CustomUserDetails;
import com.io.github.Sp1rit1.poker_server.dto.FriendCodeRequestDto;
import com.io.github.Sp1rit1.poker_server.dto.UserFriendDto; // <-- ДОБАВЬТЕ ИМПОРТ
import com.io.github.Sp1rit1.poker_server.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/add")
    public ResponseEntity<?> addFriend(@Valid @RequestBody FriendCodeRequestDto requestDto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "User not authenticated properly."));
        }

        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUserDetails.getId();

        friendService.addFriend(currentUserId, requestDto.getFriendCode());
        return ResponseEntity.ok(java.util.Map.of("message", "Friend added successfully."));
    }

    @GetMapping("/list")
    public ResponseEntity<?> getFriendList(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "User not authenticated properly."));
        }

        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUserDetails.getId();

        List<UserFriendDto> friends = friendService.getFriends(currentUserId);
        return ResponseEntity.ok(friends);
    }

    @DeleteMapping("/remove-by-code/{friendCodeToRemove}")
    public ResponseEntity<?> removeFriendByCode(@PathVariable String friendCodeToRemove, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "User not authenticated properly."));
        }

        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = currentUserDetails.getId();

        // Проверяем, не пытается ли пользователь удалить себя по своему же коду (маловероятно, но для полноты)
        if (currentUserDetails.getFriendCode() != null && currentUserDetails.getFriendCode().equals(friendCodeToRemove)) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "You cannot remove yourself using your own friend code."));
        }

        try {
            friendService.removeFriendByCode(currentUserId, friendCodeToRemove);
            return ResponseEntity.ok(java.util.Map.of("message", "Friend removed successfully using friend code."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}