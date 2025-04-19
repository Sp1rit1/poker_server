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

@Service // данный класс принадлежит к слою бизнес-логики
@RequiredArgsConstructor // генерирует конструктор с аргументами, являющимися неинициализированными final полями
public class UserService {

    // final поля для внедрения зависимостей через конструктор
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional // указывает Spring обернуть данный метод в транзакцию БД
    public User registerUser(UserRegistrationDto registrationDto) { // метод регистрации пользователя, принимающий в параметры объект, передающий регистрационные данные
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) { // проверяем с помощью репозитория есть ли в БД пользователь с таким же именем
            throw new RuntimeException("Username '" + registrationDto.getUsername() + "' already exists");
        }

        User newUser = new User(); // создаём экземпляр сущности User, соответствующий строке в таблице users в БД
        newUser.setUsername(registrationDto.getUsername()); // устанавливаем имя пользователя
        newUser.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword())); // хэшируем и устанавливаем пароль
        newUser.setEmail(registrationDto.getEmail()); // устанавливаем email
        // createdAt установится автоматически через @PrePersist
        return userRepository.save(newUser); // сохраняем пользователя в БД с помощью репозитория
    }

    @Transactional(readOnly = true) // оптимизация "только для чтения"
    public AuthResponseDto authenticateUser(LoginRequestDto loginDto) { // принимаем в параметры объект, передающий данные для входа
        User user = userRepository.findByUsername(loginDto.getUsername()) // пытаемся найти пользователя с соответствующим именем
                // переменная user получает значение типа User, возвращаемое методом orElseThrow(), вызванного у Optional<User>, возвращённого findByUsername, если Optional<User> окажется пустым, то orElseThrow() выбросит исключение
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) { // если хэши паролей не сошлись, то выбрасываем исключение
            throw new RuntimeException("Invalid username or password");
        }
        return new AuthResponseDto(user.getId(), user.getUsername()); // возвращаем объект для передачи данных об аунтефикации
    }
}