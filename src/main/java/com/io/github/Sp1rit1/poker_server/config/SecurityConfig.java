package com.io.github.Sp1rit1.poker_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Для disable CSRF
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // помечаем класс как источник конфигурации бинов
@EnableWebSecurity // включаем базовую конфигурацию Spring Security
public class SecurityConfig {

    @Bean // аннотация уровня метода, позволяющая методу явно создавать и конфифигурировать бины ( возвращаемый объект будет бином)
    public PasswordEncoder passwordEncoder() { // определяем бин для шифрования пароля для дальнейшего внедрения в UserService
        // возвращаем объект кодировщик, который будет хэшировать пароль c помощью BCrypt (крутой алгорит с встроенной "солью" (доп. строка для защиты от радужных таблиц))
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {    // основной бин, конфигурирующий правила безопасности для WEB-запросов
        http
                .csrf(AbstractHttpConfigurer::disable)  // отключаем защиту от CSRF-атак актуальных только для браузерных приложений
                .authorizeHttpRequests(authz -> authz // конфигурируем правила авторизации для HTTP-запросов
                        .requestMatchers("/api/auth/**").permitAll() // запросы, соответствущие указанному шаблону разрешены всем
                        .anyRequest().authenticated() // любой запрос не соответствующий шаблону требует, чтобы пользователь был аутентифицирован
                );

        return http.build(); // возвращаем полностью сконфигурированный объект SecurityFilterChain
    }
}
