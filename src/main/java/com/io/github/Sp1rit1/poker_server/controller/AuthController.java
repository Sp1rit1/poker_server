package com.io.github.Sp1rit1.poker_server.controller;

import com.io.github.Sp1rit1.poker_server.dto.AuthResponseDto;
import com.io.github.Sp1rit1.poker_server.dto.LoginRequestDto;
import com.io.github.Sp1rit1.poker_server.dto.UserRegistrationDto;
import com.io.github.Sp1rit1.poker_server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException; // Оставляем для специфичной обработки
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import com.io.github.Sp1rit1.poker_server.config.CustomUserDetails; // Убедитесь, что импорт правильный
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        // try-catch для RuntimeException теперь не нужен, его обработает GlobalExceptionHandler
        userService.registerUser(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        // Если userService.registerUser бросит RuntimeException (например, "Username already exists"),
        // GlobalExceptionHandler его перехватит и вернет 400 Bad Request с сообщением.
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginDto, HttpServletRequest request) {
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginDto.getUsername(),
                    loginDto.getPassword()
            );
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            AuthResponseDto authResponse = new AuthResponseDto(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getFriendCode()
            );
            return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException e) {
            // Оставляем try-catch для AuthenticationException, чтобы вернуть специфичный статус 401
            // и кастомное сообщение (или можно и его перенести в GlobalExceptionHandler).
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
        // try-catch для общего RuntimeException теперь не нужен
    }
}