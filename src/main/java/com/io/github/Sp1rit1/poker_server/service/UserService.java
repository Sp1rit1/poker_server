package com.io.github.Sp1rit1.poker_server.service;

import com.io.github.Sp1rit1.poker_server.dto.AuthResponseDto;
import com.io.github.Sp1rit1.poker_server.dto.LoginRequestDto;
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
import java.util.Base64;

@Service // данный класс принадлежит к слою бизнес-логики
@RequiredArgsConstructor // генерирует конструктор с аргументами, являющимися неинициализированными final полями
public class UserService {

    // final поля для внедрения зависимостей через конструктор
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserStatsRepository userStatsRepository; // <-- ДОБАВЛЕНА ЗАВИСИМОСТЬ

    // Статические поля для генератора кода
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    // Вспомогательный метод для генерации уникального кода дружбы
    private String generateUniqueFriendCode() {
        String code;
        int attempts = 0; // Счетчик попыток, чтобы избежать бесконечного цикла в редких случаях
        final int maxAttempts = 10; // Максимальное количество попыток генерации

        do {
            byte[] randomBytes = new byte[6]; // 6 байт дадут 8 символов Base64URL без паддинга
            secureRandom.nextBytes(randomBytes);
            code = base64Encoder.encodeToString(randomBytes).toUpperCase(); // Пример: "AB3DE8GH"
            // Можно дополнительно обрезать или изменить, если нужна строго определенная длина, например 8 символов
            // if (code.length() > 8) code = code.substring(0, 8);

            if (userRepository.findByFriendCode(code).isPresent()) {
                attempts++;
                if (attempts >= maxAttempts) {
                    // Очень маловероятно, но это защита от бесконечного цикла
                    throw new RuntimeException("Failed to generate a unique friend code after " + maxAttempts + " attempts.");
                }
                code = null; // Сбрасываем код, чтобы цикл продолжился
            }
        } while (code == null);
        return code;
    }


    @Transactional // Транзакция важна для каскадных операций
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
        newUser.setBalance(BigDecimal.valueOf(1000.00)); // Установка начального баланса

        UserStats newUserStats = new UserStats();

        newUser.setUserStats(newUserStats);

        User savedUser = userRepository.save(newUser);

        return savedUser;
    }

    @Transactional(readOnly = true) // оптимизация "только для чтения"
    public AuthResponseDto authenticateUser(LoginRequestDto loginDto) { // принимаем в параметры объект, передающий данные для входа
        User user = userRepository.findByUsername(loginDto.getUsername()) // пытаемся найти пользователя с соответствующим именем
                // переменная user получает значение типа User, возвращаемое методом orElseThrow(), вызванного у Optional<User>, возвращённого findByUsername, если Optional<User> окажется пустым, то orElseThrow() выбросит исключение
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) { // если хеши паролей не сошлись, то выбрасываем исключение
            throw new RuntimeException("Invalid username or password");
        }
        // Возвращаем объект для передачи данных об аутентификации, включая friendCode
        return new AuthResponseDto(user.getId(), user.getUsername(), user.getFriendCode()); // <-- ДОБАВЛЕН FRIEND_CODE В ОТВЕТ
    }
}