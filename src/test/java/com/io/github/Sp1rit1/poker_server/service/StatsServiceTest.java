package com.io.github.Sp1rit1.poker_server.service;

import com.io.github.Sp1rit1.poker_server.dto.UserStatsDto;
import com.io.github.Sp1rit1.poker_server.entity.User;
import com.io.github.Sp1rit1.poker_server.entity.UserStats;
import com.io.github.Sp1rit1.poker_server.repository.UserRepository;
import com.io.github.Sp1rit1.poker_server.repository.UserStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException; // Используем это исключение, так как оно выбрасывается из вашего сервиса
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private UserStatsRepository userStatsRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StatsService statsService;

    private User testUser;
    private UserStats existingUserStats;
    private final Long testUserId = 1L;
    private final String testUsername = "testUser";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername(testUsername);
        // Другие поля User можно установить, если они влияют на конструктор UserStats

        existingUserStats = new UserStats(testUser); // Используем конструктор, который связывает User
        existingUserStats.setHandsPlayed(10);
        existingUserStats.setHandsWon(5);
        // lastUpdated будет установлено автоматически через @PrePersist/@PreUpdate,
        // но в моке save() это не сработает, поэтому для тестов можно его не проверять или установить явно.
    }

    // --- Тесты для getPlayerStats ---

    @Test
    void getPlayerStats_whenStatsExist_shouldReturnUserStatsDto() {
        // Arrange
        when(userStatsRepository.findById(testUserId)).thenReturn(Optional.of(existingUserStats));

        // Act
        UserStatsDto resultDto = statsService.getPlayerStats(testUserId);

        // Assert
        assertNotNull(resultDto);
        assertEquals(testUserId, resultDto.getUserId()); // Предполагая, что UserStatsDto имеет getUserId()
        assertEquals(10, resultDto.getHandsPlayed());
        assertEquals(5, resultDto.getHandsWon());
        verify(userStatsRepository, times(1)).findById(testUserId);
        verify(userRepository, never()).findById(anyLong()); // Не должен искать User, так как статистика найдена
        verify(userStatsRepository, never()).save(any(UserStats.class)); // Не должен сохранять, только чтение
    }

    @Test
    void getPlayerStats_whenStatsNotExistAndUserExists_shouldCreateAndReturnDefaultStatsDto() {
        // Arrange
        when(userStatsRepository.findById(testUserId)).thenReturn(Optional.empty()); // Статистики нет
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));    // Пользователь есть

        // Мокируем save, чтобы он вернул тот объект, который ему передали (для проверки)
        // Это важно, так как getOrCreateUserStats сохраняет новые статсы
        ArgumentCaptor<UserStats> statsCaptor = ArgumentCaptor.forClass(UserStats.class);
        when(userStatsRepository.save(statsCaptor.capture())).thenAnswer(invocation -> statsCaptor.getValue());

        // Act
        UserStatsDto resultDto = statsService.getPlayerStats(testUserId);

        // Assert
        assertNotNull(resultDto);
        assertEquals(testUserId, resultDto.getUserId());
        assertEquals(0, resultDto.getHandsPlayed(), "Newly created stats should have 0 hands played");
        assertEquals(0, resultDto.getHandsWon(), "Newly created stats should have 0 hands won");

        verify(userStatsRepository, times(1)).findById(testUserId); // Попытка найти статистику
        verify(userRepository, times(1)).findById(testUserId);    // Поиск пользователя для создания статистики
        verify(userStatsRepository, times(1)).save(any(UserStats.class)); // Сохранение новой статистики

        UserStats savedStats = statsCaptor.getValue();
        assertNotNull(savedStats);
        assertEquals(testUser, savedStats.getUser());
        assertEquals(testUserId, savedStats.getUserId());
    }

    @Test
    void getPlayerStats_whenStatsNotExistAndUserNotExist_shouldThrowRuntimeException() {
        // Arrange
        when(userStatsRepository.findById(testUserId)).thenReturn(Optional.empty()); // Статистики нет
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());    // Пользователя тоже нет

        // Act & Assert
        // Ваш getOrCreateUserStats выбрасывает RuntimeException, если User не найден
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            statsService.getPlayerStats(testUserId);
        });
        assertTrue(exception.getMessage().contains("User not found with ID: " + testUserId));
        verify(userStatsRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).findById(testUserId);
        verify(userStatsRepository, never()).save(any(UserStats.class));
    }


    // --- Тесты для incrementHandsPlayed ---

    @Test
    void incrementHandsPlayed_whenStatsExist_shouldIncrementAndSave() {
        // Arrange
        when(userStatsRepository.findById(testUserId)).thenReturn(Optional.of(existingUserStats));
        // Мокируем save, чтобы не было реального сохранения, но мы могли проверить, что он вызван
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(existingUserStats); // Возвращаем тот же объект для простоты

        int initialHandsPlayed = existingUserStats.getHandsPlayed();

        // Act
        statsService.incrementHandsPlayed(testUserId);

        // Assert
        ArgumentCaptor<UserStats> statsCaptor = ArgumentCaptor.forClass(UserStats.class);
        verify(userStatsRepository, times(1)).save(statsCaptor.capture());
        UserStats savedStats = statsCaptor.getValue();

        assertEquals(initialHandsPlayed + 1, savedStats.getHandsPlayed());
        verify(userStatsRepository, times(1)).findById(testUserId);
    }

    @Test
    void incrementHandsPlayed_whenStatsNotExistAndUserExists_shouldCreateIncrementAndSave() {
        // Arrange
        when(userStatsRepository.findById(testUserId)).thenReturn(Optional.empty());
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        ArgumentCaptor<UserStats> statsCaptor = ArgumentCaptor.forClass(UserStats.class);
        when(userStatsRepository.save(statsCaptor.capture())).thenAnswer(invocation -> statsCaptor.getValue());

        // Act
        statsService.incrementHandsPlayed(testUserId);

        // Assert
        UserStats savedStats = statsCaptor.getValue();
        assertNotNull(savedStats);
        assertEquals(1, savedStats.getHandsPlayed());
        assertEquals(testUser, savedStats.getUser());
        verify(userStatsRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).findById(testUserId);
        // save вызывается ДВАЖДЫ: один раз в getOrCreateUserStats при создании, второй раз после инкремента
        verify(userStatsRepository, times(2)).save(any(UserStats.class));
    }


    // --- Тесты для incrementHandsWon --- (Аналогично incrementHandsPlayed)

    @Test
    void incrementHandsWon_whenStatsExist_shouldIncrementAndSave() {
        // Arrange
        when(userStatsRepository.findById(testUserId)).thenReturn(Optional.of(existingUserStats));
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(existingUserStats);

        int initialHandsWon = existingUserStats.getHandsWon();

        // Act
        statsService.incrementHandsWon(testUserId);

        // Assert
        ArgumentCaptor<UserStats> statsCaptor = ArgumentCaptor.forClass(UserStats.class);
        verify(userStatsRepository, times(1)).save(statsCaptor.capture());
        UserStats savedStats = statsCaptor.getValue();
        assertEquals(initialHandsWon + 1, savedStats.getHandsWon());
    }

    @Test
    void incrementHandsWon_whenStatsNotExistAndUserExists_shouldCreateIncrementAndSave() {
        // Arrange
        when(userStatsRepository.findById(testUserId)).thenReturn(Optional.empty());
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        ArgumentCaptor<UserStats> statsCaptor = ArgumentCaptor.forClass(UserStats.class);
        when(userStatsRepository.save(statsCaptor.capture())).thenAnswer(invocation -> statsCaptor.getValue());

        // Act
        statsService.incrementHandsWon(testUserId);

        // Assert
        UserStats savedStats = statsCaptor.getValue();
        assertNotNull(savedStats);
        assertEquals(1, savedStats.getHandsWon());
        assertEquals(testUser, savedStats.getUser());
        verify(userStatsRepository, times(2)).save(any(UserStats.class));
    }
}