package com.io.github.Sp1rit1.poker_server.service;

import com.io.github.Sp1rit1.poker_server.dto.UserRegistrationDto;
import com.io.github.Sp1rit1.poker_server.entity.User;
import com.io.github.Sp1rit1.poker_server.entity.UserStats;
import com.io.github.Sp1rit1.poker_server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;


    @InjectMocks
    private UserService userService;

    private UserRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("testuser");
        registrationDto.setPassword("password123");
        registrationDto.setEmail("testuser@example.com");
    }

    @Test
    void registerUser_shouldSaveUserWithHashedPasswordAndGeneratedFriendCodeAndStats() {
        // Arrange
        String hashedPassword = "hashedPassword123";

        // Мокируем PasswordEncoder
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn(hashedPassword);

        // Мокируем UserRepository для проверки username/email и генерации friendCode
        when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registrationDto.getEmail())).thenReturn(Optional.empty());
        // Мокируем генерацию friendCode: первый вызов возвращает существующий, второй - уникальный
        when(userRepository.findByFriendCode(anyString()))
                .thenReturn(Optional.of(new User())) // Первый раз "находим" код, чтобы проверить цикл
                .thenReturn(Optional.empty());       // Второй раз не находим, код уникален

        // Мокируем userRepository.save() чтобы он возвращал переданного юзера (с ID, если бы он присваивался)
        // Для простоты просто проверим, что объект User был передан в save
        // Для более точного теста можно использовать ArgumentCaptor
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            // В реальной БД ID был бы сгенерирован. Для мока мы можем его установить или просто вернуть объект.
            // userToSave.setId(1L); // Если бы мы хотели симулировать генерацию ID
            return userToSave;
        });


        // Act
        User registeredUser = userService.registerUser(registrationDto);

        // Assert
        assertNotNull(registeredUser, "Registered user should not be null");
        assertEquals(registrationDto.getUsername(), registeredUser.getUsername(), "Username should match");
        assertEquals(hashedPassword, registeredUser.getPasswordHash(), "Password should be hashed");
        assertEquals(registrationDto.getEmail(), registeredUser.getEmail(), "Email should match");
        assertNotNull(registeredUser.getFriendCode(), "Friend code should be generated");
        assertEquals(6, registeredUser.getFriendCode().length(), "Friend code length should be correct");
        assertEquals(BigDecimal.valueOf(1000.00), registeredUser.getBalance(), "Initial balance should be set");

        // Проверяем, что UserStats был создан и связан
        assertNotNull(registeredUser.getUserStats(), "UserStats should be created and associated");
        assertEquals(registeredUser, registeredUser.getUserStats().getUser(), "UserStats should reference the new user");
        // assertEquals(registeredUser.getId(), registeredUser.getUserStats().getUserId(), "UserStats userId should match user id"); // Если ID устанавливается в моке

        // Проверяем, что userRepository.save был вызван один раз
        verify(userRepository, times(1)).save(any(User.class));
        // Проверяем, что passwordEncoder.encode был вызван один раз
        verify(passwordEncoder, times(1)).encode(registrationDto.getPassword());
        // Проверяем, что findByFriendCode был вызван как минимум дважды (один раз для симуляции коллизии, второй для успеха)
        verify(userRepository, atLeast(2)).findByFriendCode(anyString());
    }

    @Test
    void registerUser_shouldSaveUserWithCorrectFriendCodeAfterOneCollision() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Симулируем одну коллизию, затем уникальный код
        when(userRepository.findByFriendCode(anyString()))
                .thenReturn(Optional.of(new User())) // Первая попытка - код занят
                .thenReturn(Optional.empty());      // Вторая попытка - код свободен

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User registeredUser = userService.registerUser(registrationDto);

        // Assert
        assertNotNull(registeredUser.getFriendCode());
        assertEquals(6, registeredUser.getFriendCode().length());
        verify(userRepository, times(2)).findByFriendCode(anyString()); // Два вызова: один неудачный, один успешный
        verify(userRepository, times(1)).save(any(User.class));
    }


    @Test
    void registerUser_whenUsernameExists_shouldThrowRuntimeException() {
        // Arrange
        when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.of(new User()));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registrationDto);
        });
        assertEquals("Username '" + registrationDto.getUsername() + "' already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Убедимся, что save не вызывался
    }

    @Test
    void registerUser_whenEmailExists_shouldThrowRuntimeException() {
        // Arrange
        when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registrationDto.getEmail())).thenReturn(Optional.of(new User()));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registrationDto);
        });
        assertEquals("Email '" + registrationDto.getEmail() + "' is already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_whenEmailIsNull_shouldRegisterSuccessfully() {
        // Arrange
        registrationDto.setEmail(null); // Email не обязателен
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("hashedPassword");
        when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByFriendCode(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User registeredUser = userService.registerUser(registrationDto);

        // Assert
        assertNotNull(registeredUser, "User should be registered even with null email");
        assertNull(registeredUser.getEmail(), "Email should be null");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_whenEmailIsEmpty_shouldRegisterSuccessfully() {
        // Arrange
        registrationDto.setEmail(""); // Email не обязателен
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("hashedPassword");
        when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.empty());
        // findByEmail("") не должен ничего находить или не должен вызываться, если проверка на !isEmpty()
        when(userRepository.findByFriendCode(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User registeredUser = userService.registerUser(registrationDto);

        // Assert
        assertNotNull(registeredUser, "User should be registered even with empty email");
        assertEquals("", registeredUser.getEmail(), "Email should be empty");
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, never()).findByEmail(""); // Проверяем, что findByEmail не вызывался для пустой строки из-за !registrationDto.getEmail().isEmpty()
    }

    @Test
    void generateUniqueFriendCode_shouldRetryUntilUniqueOrMaxAttempts() {
        // Этот тест больше для внутренней функции, но можно проверить логику косвенно.
        // Arrange
        when(userRepository.findByFriendCode(anyString()))
                .thenReturn(Optional.of(new User())) // 1st collision
                .thenReturn(Optional.of(new User())) // 2nd collision
                .thenReturn(Optional.empty());       // 3rd is unique

        // Т.к. generateUniqueFriendCode приватный, мы не можем его вызвать напрямую.
        // Мы проверим количество вызовов findByFriendCode при регистрации.
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.registerUser(registrationDto);

        // Assert
        verify(userRepository, times(3)).findByFriendCode(anyString()); // Должно быть вызвано 3 раза
    }

    @Test
    void generateUniqueFriendCode_shouldThrowExceptionAfterMaxAttempts() {
        // Arrange
        // Заставляем findByFriendCode всегда возвращать существующего пользователя, чтобы симулировать бесконечные коллизии
        when(userRepository.findByFriendCode(anyString())).thenReturn(Optional.of(new User()));

        // Остальные моки для registerUser
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        // passwordEncoder не будет вызван, так как исключение произойдет раньше

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registrationDto);
        });
        assertTrue(exception.getMessage().startsWith("Failed to generate a unique friend code after"));
        // Проверяем, что было сделано ровно maxAttempts вызовов + 1 (начальный) перед исключением
        // В вашей реализации это будет ровно maxAttempts, так как проверка идет после генерации
        verify(userRepository, times(20)).findByFriendCode(anyString()); // 20 - это maxAttempts в вашем коде
        verify(userRepository, never()).save(any(User.class)); // Сохранение не должно произойти
    }

    @Test
    void registerUser_captorTest_verifyUserPropertiesAndStatsAssociation() {
        // Arrange
        String hashedPassword = "hashedTestPassword";
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn(hashedPassword);
        when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registrationDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByFriendCode(anyString())).thenReturn(Optional.empty());

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        // Нам не нужно мокировать userStatsRepository.save(), так как сохранение идет каскадно через user.

        // Act
        userService.registerUser(registrationDto);

        // Assert
        verify(userRepository).save(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();

        assertNotNull(capturedUser, "Captured user should not be null");
        assertEquals(registrationDto.getUsername(), capturedUser.getUsername());
        assertEquals(hashedPassword, capturedUser.getPasswordHash());
        assertEquals(registrationDto.getEmail(), capturedUser.getEmail());
        assertNotNull(capturedUser.getFriendCode());
        assertEquals(6, capturedUser.getFriendCode().length());
        assertEquals(BigDecimal.valueOf(1000.00), capturedUser.getBalance());

        // Проверяем UserStats
        UserStats capturedUserStats = capturedUser.getUserStats();
        assertNotNull(capturedUserStats, "Captured user stats should not be null");
        // ID у UserStats будет null до фактического сохранения User,
        // но связь User -> UserStats и UserStats -> User должна быть установлена
        assertEquals(capturedUser, capturedUserStats.getUser(), "UserStats should correctly reference its User");
        // assertEquals(0, capturedUserStats.getHandsPlayed()); // Проверка начальных значений, если они есть
    }
}