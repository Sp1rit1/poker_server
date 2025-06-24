package com.io.github.Sp1rit1.poker_server.config; // Убедитесь, что пакет ваш. Если JwtAuthenticationFilter в другом, добавьте импорт.

// import com.io.github.Sp1rit1.poker_server.service.MyUserDetailsService; // Не нужен здесь, если используется только AuthenticationManager'ом
import com.io.github.Sp1rit1.poker_server.security.jwt.JwtAuthenticationFilter; // <-- НОВЫЙ ИМПОРТ
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy; // Для STATELESS
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Для addFilterBefore

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // <-- ВНЕДРЯЕМ НАШ JWT ФИЛЬТР

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF, так как у нас stateless API с JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Устанавливаем политику управления сессиями на STATELESS
                // Сервер не будет создавать или использовать HTTP сессии для хранения SecurityContext
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Правила авторизации запросов
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll() // Регистрация и логин доступны всем
                        .requestMatchers("/api/friends/**").authenticated() // Требуют валидного JWT
                        .requestMatchers("/api/stats/**").authenticated()   // Требуют валидного JWT
                        .anyRequest().authenticated() // Все остальные запросы требуют валидного JWT
                )

                // Добавляем наш JwtAuthenticationFilter перед стандартным фильтром UsernamePasswordAuthenticationFilter
                // Этот фильтр будет проверять JWT в каждом запросе и устанавливать аутентификацию
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}