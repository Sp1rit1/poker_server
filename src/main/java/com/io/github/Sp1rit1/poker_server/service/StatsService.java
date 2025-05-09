package com.io.github.Sp1rit1.poker_server.service;

import com.io.github.Sp1rit1.poker_server.entity.User; // Импорт вашей сущности User
import com.io.github.Sp1rit1.poker_server.entity.UserStats; // Импорт вашей сущности UserStats
import com.io.github.Sp1rit1.poker_server.repository.UserRepository; // Импорт вашего UserRepository
import com.io.github.Sp1rit1.poker_server.repository.UserStatsRepository; // Импорт вашего UserStatsRepository
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * (Пример) Добавляет сумму к общему выигрышу пользователя.
     * Раскомментируйте и адаптируйте, если вам нужен такой функционал.
     * Убедитесь, что поле totalWinnings существует в UserStats и имеет тип BigDecimal.
     *
     * @param userId ID пользователя.
     * @param amount Сумма выигрыша.
     */
    /*
    @Transactional
    public void addWinnings(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            // Не добавляем нулевые или отрицательные выигрыши, или обрабатываем по-другому
            return;
        }
        UserStats stats = getOrCreateUserStats(userId);
        if (stats.getTotalWinnings() == null) { // На случай если поле может быть null
            stats.setTotalWinnings(BigDecimal.ZERO);
        }
        stats.setTotalWinnings(stats.getTotalWinnings().add(amount));
        userStatsRepository.save(stats);
    }
    */

    // Вы можете добавить здесь другие методы для обновления различных статистических показателей:
    // - Количество сделанных фолдов, чеков, коллов, бетов, рейзов
    // - Самый большой выигранный банк
    // - И т.д.
}
