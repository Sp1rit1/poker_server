package com.io.github.Sp1rit1.poker_server.security.jwt;

import com.io.github.Sp1rit1.poker_server.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // Не обязательно, если не используем @Mock, @InjectMocks
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private final String testSecret = "TestSecretKeyForJWTTokensWhichIsDefinitelyLongEnoughAndSecure32Bytes"; // > 256 бит
    private final int testExpirationMs = 60000; // 1 минута

    private CustomUserDetails userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // Используем ReflectionTestUtils для установки значений, аннотированных @Value,
        // так как в юнит-тесте Spring контекст не внедряет их.
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecretString", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", testExpirationMs);
        jwtTokenProvider.init(); // Вызываем init() вручную, так как @PostConstruct не сработает

        userDetails = new CustomUserDetails(
                1L,
                "testuser",
                "password",
                "TESTFC",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                true, true, true, true
        );
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtTokenProvider.validateToken(token), "Generated token should be valid");
    }

    @Test
    void getUsernameFromJwt_shouldReturnCorrectUsername() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        String usernameFromToken = jwtTokenProvider.getUsernameFromJwt(token);

        // Assert
        assertEquals(userDetails.getUsername(), usernameFromToken);
    }

    @Test
    void getUserIdFromJwt_shouldReturnCorrectUserId() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Long userIdFromToken = jwtTokenProvider.getUserIdFromJwt(token);

        // Assert
        assertEquals(userDetails.getId(), userIdFromToken);
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }



    @Test
    void validateToken_withEmptyToken_shouldReturnFalse() {
        // Arrange
        String emptyToken = "";
        // Act
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);
        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_withNullToken_shouldReturnFalse() {
        // Arrange
        String nullToken = null;
        // Act
        boolean isValid = jwtTokenProvider.validateToken(nullToken);
        // Assert
        assertFalse(isValid);
    }

    @Test
    void init_whenSecretIsShort_shouldGenerateKeyAndLogWarning() {
        // Этот тест немного сложнее, так как нужно проверить лог.
        // Можно использовать Appender для SLF4J или просто проверить, что ключ сгенерирован.
        // Для простоты проверим, что jwtSecretKey не null после init с коротким секретом.
        JwtTokenProvider shortSecretProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortSecretProvider, "jwtSecretString", "short");
        ReflectionTestUtils.setField(shortSecretProvider, "jwtExpirationMs", testExpirationMs);

        // Act
        shortSecretProvider.init();

        // Assert
        Object secretKeyField = ReflectionTestUtils.getField(shortSecretProvider, "jwtSecretKey");
        assertNotNull(secretKeyField, "jwtSecretKey should be generated even if jwtSecretString is short");
        // Проверка лога требует более сложной настройки (например, TestAppender).
    }

    @Test
    void init_whenSecretIsValid_shouldUseIt() {
        JwtTokenProvider validSecretProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(validSecretProvider, "jwtSecretString", testSecret); // Используем длинный тестовый секрет
        ReflectionTestUtils.setField(validSecretProvider, "jwtExpirationMs", testExpirationMs);

        // Act
        validSecretProvider.init();

        // Assert
        // Мы не можем легко сравнить ключи, но можем убедиться, что он не null
        // и что не было предупреждения о генерации (это сложнее проверить в юнит-тесте)
        Object secretKeyField = ReflectionTestUtils.getField(validSecretProvider, "jwtSecretKey");
        assertNotNull(secretKeyField, "jwtSecretKey should be initialized from valid jwtSecretString");
    }
}