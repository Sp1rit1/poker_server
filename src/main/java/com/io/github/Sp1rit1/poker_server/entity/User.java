package com.io.github.Sp1rit1.poker_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false, updatable = false) // значение этого поля не должно включаться в SQL UPDATE запросы, оно устанавливается 1 раз при создании
    private LocalDateTime createdAt;

    @PrePersist // указывает на то, что метод должен быть вызван перед тем как новая сущность будет впервые сохранена (перед INSERT)
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}