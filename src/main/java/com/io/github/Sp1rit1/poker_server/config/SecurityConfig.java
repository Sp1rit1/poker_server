package com.io.github.Sp1rit1.poker_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Для disable CSRF
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean // Определяем бин для хеширования паролей
    public PasswordEncoder passwordEncoder() {
        // Используем BCrypt - стандартный надежный алгоритм
        return new BCryptPasswordEncoder();
    }

    @Bean // Определяем цепочку фильтров безопасности для HTTP запросов
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF-защиту, т.к. она обычно не нужна для REST API
                .csrf(AbstractHttpConfigurer::disable)
                // Настраиваем правила авторизации для запросов
                .authorizeHttpRequests(authz -> authz
                        // Разрешаем всем доступ к эндпоинтам /api/auth/** (регистрация, логин)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                );
        // В будущем здесь можно будет добавить конфигурацию JWT или других методов аутентификации

        return http.build(); // Собираем и возвращаем настроенную цепочку фильтров
    }
}
