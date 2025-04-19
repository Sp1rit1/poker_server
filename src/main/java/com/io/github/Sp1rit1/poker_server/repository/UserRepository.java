package com.io.github.Sp1rit1.poker_server.repository;

import com.io.github.Sp1rit1.poker_server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository // Опционально, спринг сам определяет репозитории, наследуемые от JPARepository
public interface UserRepository extends JpaRepository<User, Long> { // интерфейс для работы с таблицой users посредством сущности User
    // наследуясь от JPARepository получаем все базовые методы (CRUD-операции, save(), findAll() и др.)
    Optional<User> findByUsername(String username); // производный метод запроса (Spring Data JPA парсит имя метода и создаёт соответствующий JPQL (или SQL) запрос
    // Optional - класс-контейнер, содеражащий либо объект User, либо пуст
}

// не нужно создавать класс, реализующий интерфейс UserRepository, потому что Spring Data JPA делает это автоматически во время запуска приложения.