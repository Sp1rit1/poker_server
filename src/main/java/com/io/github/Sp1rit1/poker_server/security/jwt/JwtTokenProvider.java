package com.io.github.Sp1rit1.poker_server.security.jwt; // Или ваш выбранный пакет

import com.io.github.Sp1rit1.poker_server.security.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct; // Для инициализации ключа
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}") // Секретный ключ будет браться из application.properties
    private String jwtSecretString;

    @Value("${app.jwt.expiration-ms}") // Время жизни токена в миллисекундах
    private int jwtExpirationMs;

    private SecretKey jwtSecretKey;

    @PostConstruct
    protected void init() {
        // Генерируем безопасный ключ на основе секрета из properties.
        // Для продакшена секрет должен быть достаточно сложным и длинным.
        // Если jwtSecretString достаточно длинный и безопасный, можно использовать его напрямую:
        // this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecretString.getBytes(StandardCharsets.UTF_8));
        // Для простоты примера, если строка короткая, лучше сгенерировать ключ:
        if (jwtSecretString == null || jwtSecretString.length() < 32) { // 32 байта = 256 бит
            logger.warn("JWT secret is not configured or too short in application.properties. Generating a temporary key. THIS IS NOT SECURE FOR PRODUCTION.");
            this.jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Генерирует случайный ключ HS256
        } else {
            this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecretString.getBytes());
        }
    }

    public String generateToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // Собираем роли (authorities) в строку, если они есть и нужны в токене
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // Обычно сюда кладут username
                .claim("userId", userPrincipal.getId()) // Добавляем кастомные данные (claims)
                .claim("friendCode", userPrincipal.getFriendCode())
                .claim("authorities", authorities) // Можно добавить роли, если они используются
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256) // Подписываем токен
                .compact();
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Long getUserIdFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Long.class); // Извлекаем userId
    }


    public boolean validateToken(String authToken) {
        if (authToken == null || authToken.isEmpty()) {
            return false;
        }
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
}