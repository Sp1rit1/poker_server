package com.io.github.Sp1rit1.poker_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity // данный класс является сущностью, которая соответствует таблице users из БД
@Table(name = "users")

public class User {

    @Id // указывает, что данное поле является первичным ключом
    @GeneratedValue(strategy = GenerationType.IDENTITY) // указывает стратегию генерации значения ID, в данном случае (IDENTITY) делегирует эту задачу базе данных
    private Long id;

    @Column(nullable = false, unique = true) // указывает, что данное поле соответствует одноимённому столбцу, не может быть NULL и значения в ней должны быть уникальны
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(precision = 10, scale = 2)
    private BigDecimal balance; // Баланс игрока

    @Column(unique = true, length = 6) // Код дружбы, уникальный, длина 6
    private String friendCode;

    @Column(nullable = false, updatable = false) // значение этого поля не должно включаться в SQL UPDATE запросы, оно устанавливается 1 раз при создании
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserStats userStats;


    // Вспомогательный метод для удобного добавления статистики (поддерживает двунаправленную связь)
    public void setUserStats(UserStats stats) {
        if (stats == null) {
            if (this.userStats != null) {
                this.userStats.setUser(null); // Разорвать связь со старой статистикой
            }
        } else {
            stats.setUser(this); // Установить User в статистике
        }
        this.userStats = stats; // Установить статистику в User
    }

    @PrePersist // указывает на то, что метод должен быть вызван перед тем как новая сущность будет впервые сохранена (перед INSERT)
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}