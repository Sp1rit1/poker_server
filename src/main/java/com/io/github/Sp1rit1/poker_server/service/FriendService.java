package com.io.github.Sp1rit1.poker_server.service;

import com.io.github.Sp1rit1.poker_server.entity.User;
import com.io.github.Sp1rit1.poker_server.entity.UserFriend;
import com.io.github.Sp1rit1.poker_server.entity.UserFriendId;
import com.io.github.Sp1rit1.poker_server.repository.UserFriendRepository;
import com.io.github.Sp1rit1.poker_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor // Lombok: для внедрения зависимостей через конструктор
public class FriendService {

    private final UserRepository userRepository;
    private final UserFriendRepository userFriendRepository;

    @Transactional
    public void addFriend(Long currentUserId, String friendCodeToAdd) {
        // 1. Найти пользователя, которого хотят добавить в друзья, по его коду
        User userToAdd = userRepository.findByFriendCode(friendCodeToAdd)
                .orElseThrow(() -> new RuntimeException("User with friend code '" + friendCodeToAdd + "' not found."));

        // 2. Проверить, не пытается ли пользователь добавить сам себя
        if (currentUserId.equals(userToAdd.getId())) {
            throw new RuntimeException("You cannot add yourself as a friend.");
        }

        // 3. Получить текущего пользователя (инициатора запроса)
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found. This should not happen.")); // Маловероятная ошибка, если ID из токена

        // 4. Проверить, не являются ли они уже друзьями
        // Создаем ID для проверки в обоих направлениях, если ваша структура UserFriendId это требует
        // Или используйте метод репозитория areFriends, если он есть
        UserFriendId id1 = new UserFriendId(currentUser.getId(), userToAdd.getId());
        UserFriendId id2 = new UserFriendId(userToAdd.getId(), currentUser.getId());

        if (userFriendRepository.existsById(id1) || userFriendRepository.existsById(id2)) {
            throw new RuntimeException("You are already friends with this user.");
        }

        // 5. Создать запись о дружбе.
        // Мы создаем одну запись, где ID пользователей упорядочены (например, меньший ID первый),
        // чтобы избежать дублирования (A-B и B-A как разные записи).
        // Либо, если вы хотите хранить две симметричные записи, создайте обе.
        // Для простоты, предположим, что UserFriendId всегда хранит (меньшийId, большийId)
        // или ваша логика запросов это обрабатывает.

        UserFriendId friendshipId;
        User friend1Entity;
        User friend2Entity;

        if (currentUser.getId() < userToAdd.getId()) {
            friendshipId = new UserFriendId(currentUser.getId(), userToAdd.getId());
            friend1Entity = currentUser;
            friend2Entity = userToAdd;
        } else {
            friendshipId = new UserFriendId(userToAdd.getId(), currentUser.getId());
            friend1Entity = userToAdd;
            friend2Entity = currentUser;
        }

        UserFriend newFriendship = new UserFriend();
        newFriendship.setId(friendshipId);
        newFriendship.setUser1(friend1Entity); // Пользователь с меньшим ID (или первый)
        newFriendship.setUser2(friend2Entity); // Пользователь с большим ID (или второй)
        newFriendship.setStatus("ACCEPTED"); // Сразу принимаем дружбу для MVP

        userFriendRepository.save(newFriendship);

        // Опционально: здесь можно было бы реализовать систему запросов в друзья (статус PENDING)
        // и уведомления, но для MVP делаем простое добавление.
    }

    // Другие методы, связанные с друзьями, могут быть здесь (например, удалить друга, получить список друзей)
}
