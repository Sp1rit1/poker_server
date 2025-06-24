package com.io.github.Sp1rit1.poker_server.service;

import com.io.github.Sp1rit1.poker_server.entity.User; // Импорт вашей сущности User
import com.io.github.Sp1rit1.poker_server.entity.UserStats; // Импорт вашей сущности UserStats
import com.io.github.Sp1rit1.poker_server.repository.UserRepository; // Импорт вашего UserRepository
import com.io.github.Sp1rit1.poker_server.repository.UserStatsRepository; // Импорт вашего UserStatsRepository
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.io.github.Sp1rit1.poker_server.dto.UserStatsDto; // <--- ДОБАВЬТЕ ЭТОТ ИМПОРТ
import com.io.github.Sp1rit1.poker_server.entity.UserStats;  // Убедитесь, что он уже есть

import java.math.BigDecimal; // Если будете использовать для выигрышей

@Service
@RequiredArgsConstructor // Lombok: автоматически создает конструктор для final полей (для внедрения зависимостей)
public class StatsService {

    private final UserStatsRepository userStatsRepository;
    private final UserRepository userRepository; // Нужен, чтобы получить объект User для создания UserStats, если его еще нет

    /**
     * Вспомогательный приватный метод для получения существующей статистики пользователя
     * или создания новой, если она еще не существует.
     *
     * @param userId ID пользователя, для которого нужно получить/создать статистику.
     * @return объект UserStats для данного пользователя.
     * @throws RuntimeException если пользователь с указанным ID не найден (для создания новой статистики).
     */
    private UserStats getOrCreateUserStats(Long userId) {
        // Пытаемся найти статистику по userId. userStats.userId является первичным ключом.
        return userStatsRepository.findById(userId)
                .orElseGet(() -> { // Если статистика не найдена, создаем новую
                    // Находим пользователя, чтобы связать с ним статистику
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId + " while trying to create stats."));

                    // Создаем новый объект UserStats, связывая его с пользователем
                    // Предполагается, что у UserStats есть конструктор, принимающий User,
                    // или что связь устанавливается через userStats.setUser(user) и userStats.setUserId(user.getId())
                    UserStats newStats = new UserStats(user); // Используем конструктор UserStats(User user), который должен установить user и userId

                    // Сохраняем новую запись статистики в базе данных
                    return userStatsRepository.save(newStats);
                });
    }

    /**
     * Увеличивает счетчик сыгранных рук для указанного пользователя.
     * Операция выполняется в транзакции.
     *
     * @param userId ID пользователя.
     */
    @Transactional // Гарантирует, что чтение и запись статистики будут атомарны
    public void incrementHandsPlayed(Long userId) {
        UserStats stats = getOrCreateUserStats(userId); // Получаем или создаем статистику
        stats.setHandsPlayed(stats.getHandsPlayed() + 1); // Увеличиваем счетчик
        userStatsRepository.save(stats); // Сохраняем изменения
        // System.out.println("User " + userId + " hands played: " + stats.getHandsPlayed()); // Для отладки
    }

    /**
     * Увеличивает счетчик выигранных рук для указанного пользователя.
     * Операция выполняется в транзакции.
     *
     * @param userId ID пользователя.
     */
    @Transactional
    public void incrementHandsWon(Long userId) {
        UserStats stats = getOrCreateUserStats(userId);
        stats.setHandsWon(stats.getHandsWon() + 1);
        userStatsRepository.save(stats);
        // System.out.println("User " + userId + " hands won: " + stats.getHandsWon()); // Для отладки
    }

    @Transactional(readOnly = true)
    public UserStatsDto getPlayerStats(Long userId) {
        UserStats stats = getOrCreateUserStats(userId); // Используем ваш существующий метод

        // Создаем и возвращаем DTO
        UserStatsDto statsDto = new UserStatsDto();
        statsDto.setUserId(stats.getUserId());
        statsDto.setHandsPlayed(stats.getHandsPlayed());
        statsDto.setHandsWon(stats.getHandsWon());
        // Установите здесь другие поля DTO из сущности stats, если вы их добавили
        // например, statsDto.setTotalWinnings(stats.getTotalWinnings());

        return statsDto;
    }
}
