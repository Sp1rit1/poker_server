package com.io.github.Sp1rit1.poker_server.repository; // Замените на ваш пакет!

import com.io.github.Sp1rit1.poker_server.entity.User; // Импорт вашей User Entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
// import org.springframework.stereotype.Repository; // Можно раскомментировать

import java.util.Optional;


@Repository // Опционально
public interface UserRepository extends JpaRepository<User, Long> { // <Тип Сущности, Тип ID>

    /**
     * Находит пользователя по его имени пользователя (username).
     * Spring Data JPA автоматически генерирует реализацию этого метода.
     *
     * @param username Имя пользователя для поиска.
     * @return Optional, содержащий найденного пользователя, или пустой Optional, если пользователь не найден.
     */

    Optional<User> findByUsername(String username);

    // --- Другие возможные методы (пока не нужны) ---
    // Optional<User> findByEmail(String email);
    // boolean existsByUsername(String username);
    // boolean existsByEmail(String email);

}