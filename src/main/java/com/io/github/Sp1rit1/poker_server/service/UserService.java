package com.io.github.Sp1rit1.poker_server.service;

import com.io.github.Sp1rit1.poker_server.dto.AuthResponseDto;
import com.io.github.Sp1rit1.poker_server.dto.LoginRequestDto;
import com.io.github.Sp1rit1.poker_server.dto.UserRegistrationDto;
import com.io.github.Sp1rit1.poker_server.entity.User;
import com.io.github.Sp1rit1.poker_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Для транзакций

@Service // Помечаем как сервис Spring
@RequiredArgsConstructor // Lombok для внедрения зависимостей через конструктор
public class UserService {

    private final UserRepository userRepository; // Репозиторий для доступа к данным User
    private final PasswordEncoder passwordEncoder; // Бин для хеширования паролей

    @Transactional // Операции внутри метода - в одной транзакции
    public User registerUser(UserRegistrationDto registrationDto) {
        // 1. Проверка, не занято ли имя пользователя
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            // В реальном приложении лучше бросать кастомное исключение
            throw new RuntimeException("Username '" + registrationDto.getUsername() + "' already exists");
        }
        // Опционально: Проверка email, если он уникален и обязателен

        // 2. Создание нового пользователя
        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        // 3. Хеширование пароля перед сохранением
        newUser.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));
        newUser.setEmail(registrationDto.getEmail());
        // createdAt установится автоматически через @PrePersist

        // 4. Сохранение пользователя в БД
        return userRepository.save(newUser);
    }

    @Transactional(readOnly = true) // Транзакция только для чтения (оптимизация)
    public AuthResponseDto authenticateUser(LoginRequestDto loginDto) {
        // 1. Найти пользователя по имени
        User user = userRepository.findByUsername(loginDto.getUsername())
                // Если не найден, бросить исключение
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // 2. Проверить совпадение пароля (сырой из запроса и хеш из БД)
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            // Если пароль не совпадает, бросить то же исключение
            throw new RuntimeException("Invalid username or password");
        }

        // 3. Если все хорошо, вернуть данные для ответа
        return new AuthResponseDto(user.getId(), user.getUsername());
    }
}