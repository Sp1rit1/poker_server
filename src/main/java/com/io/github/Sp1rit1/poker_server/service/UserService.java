package com.io.github.Sp1rit1.poker_server.service;

import com.io.github.Sp1rit1.poker_server.dto.UserRegistrationDto;
import com.io.github.Sp1rit1.poker_server.entity.User;
import com.io.github.Sp1rit1.poker_server.entity.UserStats;
import com.io.github.Sp1rit1.poker_server.repository.UserRepository;
import com.io.github.Sp1rit1.poker_server.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserStatsRepository userStatsRepository;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String FRIEND_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int FRIEND_CODE_LENGTH = 6;

    private String generateUniqueFriendCode() {
        String code;
        int attempts = 0;
        final int maxAttempts = 20;

        do {
            StringBuilder sb = new StringBuilder(FRIEND_CODE_LENGTH);
            for (int i = 0; i < FRIEND_CODE_LENGTH; i++) {
                int randomIndex = secureRandom.nextInt(FRIEND_CODE_CHARACTERS.length());
                sb.append(FRIEND_CODE_CHARACTERS.charAt(randomIndex));
            }
            code = sb.toString();

            if (userRepository.findByFriendCode(code).isPresent()) {
                attempts++;
                if (attempts >= maxAttempts) {
                    throw new RuntimeException("Failed to generate a unique friend code after " + maxAttempts + " attempts (collision).");
                }
                code = null;
            }
        } while (code == null);
        return code;
    }


    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username '" + registrationDto.getUsername() + "' already exists");
        }

        if (registrationDto.getEmail() != null && !registrationDto.getEmail().isEmpty() &&
                userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email '" + registrationDto.getEmail() + "' is already in use");
        }

        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));
        newUser.setEmail(registrationDto.getEmail());
        newUser.setFriendCode(generateUniqueFriendCode());
        newUser.setBalance(BigDecimal.valueOf(1000.00));

        UserStats newUserStats = new UserStats();
        newUser.setUserStats(newUserStats);

        return userRepository.save(newUser);
    }
}