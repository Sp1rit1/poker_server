package com.io.github.Sp1rit1.poker_server.service;

import com.io.github.Sp1rit1.poker_server.entity.User;
import com.io.github.Sp1rit1.poker_server.entity.UserStats;
import com.io.github.Sp1rit1.poker_server.repository.UserRepository;
import com.io.github.Sp1rit1.poker_server.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.io.github.Sp1rit1.poker_server.dto.UserStatsDto;


@Service
@RequiredArgsConstructor // Lombok: автоматически создает конструктор для final полей (для внедрения зависимостей)
public class StatsService {

    private final UserStatsRepository userStatsRepository;
    private final UserRepository userRepository; // Нужен, чтобы получить объект User для создания UserStats, если его еще нет

    private UserStats getOrCreateUserStats(Long userId) {
        // Пытаемся найти статистику по userId. userStats.userId является первичным ключом.
        return userStatsRepository.findById(userId)
                .orElseGet(() -> { // Если статистика не найдена, создаем новую
                    // Находим пользователя, чтобы связать с ним статистику
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId + " while trying to create stats."));

                    UserStats newStats = new UserStats(user);

                    // Сохраняем новую запись статистики в базе данных
                    return userStatsRepository.save(newStats);
                });
    }


    @Transactional // Гарантирует, что чтение и запись статистики будут атомарны
    public void incrementHandsPlayed(Long userId) {
        UserStats stats = getOrCreateUserStats(userId); // Получаем или создаем статистику
        stats.setHandsPlayed(stats.getHandsPlayed() + 1); // Увеличиваем счетчик
        userStatsRepository.save(stats);
    }


    @Transactional
    public void incrementHandsWon(Long userId) {
        UserStats stats = getOrCreateUserStats(userId);
        stats.setHandsWon(stats.getHandsWon() + 1);
        userStatsRepository.save(stats);
    }

    @Transactional(readOnly = true)
    public UserStatsDto getPlayerStats(Long userId) {
        UserStats stats = getOrCreateUserStats(userId);

        // Создаем и возвращаем DTO
        UserStatsDto statsDto = new UserStatsDto();
        statsDto.setUserId(stats.getUserId());
        statsDto.setHandsPlayed(stats.getHandsPlayed());
        statsDto.setHandsWon(stats.getHandsWon());

        return statsDto;
    }
}
