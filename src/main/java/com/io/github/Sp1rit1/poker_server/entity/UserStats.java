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

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // Конструктор для создания статистики для нового пользователя
    public UserStats(User user) {
        this.user = user;
        this.userId = user.getId();
        // Инициализация других полей при необходимости
    }
}