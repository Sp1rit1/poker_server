package com.io.github.Sp1rit1.poker_server.entity;

import jakarta.persistence.*; // или javax.persistence
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY) // Не нужно, т.к. ID = user_id
    private Long userId; // ID пользователя, также является первичным ключом

    @OneToOne(fetch = FetchType.LAZY) // Отношение "один-к-одному" с User
    @MapsId // Указывает, что первичный ключ этой сущности также является внешним ключом к User
    @JoinColumn(name = "user_id")
    private User user;

    private int handsPlayed = 0;
    private int handsWon = 0;
    // Добавьте другие поля статистики здесь...

    private LocalDateTime lastUpdated;

    public UserStats(User user) {
        this.user = user;               // Устанавливаем связь с User
        this.userId = user.getId();     // Устанавливаем userId из переданного User
        // Здесь можно инициализировать другие поля статистики значениями по умолчанию,
        // если они не инициализируются при объявлении поля (как handsPlayed = 0)
        // Например, this.totalWinnings = BigDecimal.ZERO;
        this.lastUpdated = LocalDateTime.now(); // Можно установить время создания/обновления
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}