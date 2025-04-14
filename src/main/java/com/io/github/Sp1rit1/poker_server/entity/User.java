package com.io.github.Sp1rit1.poker_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data // Lombok: getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: конструктор без аргументов
@AllArgsConstructor // Lombok: конструктор со всеми аргументами
@Entity // Указывает, что это JPA сущность
@Table(name = "users") // Связывает с таблицей "users" в БД

public class User {

    @Id // Первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Автогенерация ID базой данных (PostgreSQL)
    private Long id;

    @Column(nullable = false, unique = true) // Не может быть null, должно быть уникальным
    private String username;

    @Column(nullable = false) // Не может быть null
    private String passwordHash; // Храним хеш пароля

    @Column(unique = true) // Уникальное (можно сделать nullable = false, если email обязателен)
    private String email;

    // @Column // Раскомментируйте и настройте, когда добавите баланс
    // private Long balance;

    @Column(nullable = false, updatable = false) // Не null, не обновляется после создания
    private LocalDateTime createdAt;

    @PrePersist // Метод вызывается перед первым сохранением (INSERT)
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}