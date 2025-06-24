package com.io.github.Sp1rit1.poker_server.service;

import com.io.github.Sp1rit1.poker_server.entity.User;
import com.io.github.Sp1rit1.poker_server.entity.UserFriend;
import com.io.github.Sp1rit1.poker_server.entity.UserFriendId;
import com.io.github.Sp1rit1.poker_server.repository.UserFriendRepository;
import com.io.github.Sp1rit1.poker_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.io.github.Sp1rit1.poker_server.dto.UserFriendDto;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Optional;

@Service
@RequiredArgsConstructor
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
        UserFriendId id1 = new UserFriendId(currentUser.getId(), userToAdd.getId());
        UserFriendId id2 = new UserFriendId(userToAdd.getId(), currentUser.getId());

        if (userFriendRepository.existsById(id1) || userFriendRepository.existsById(id2)) {
            throw new RuntimeException("You are already friends with this user.");
        }

        // 5. Создать запись о дружбе.

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
        newFriendship.setStatus("ACCEPTED");

        userFriendRepository.save(newFriendship);
    }

    @Transactional(readOnly = true)
    public List<UserFriendDto> getFriends(Long currentUserId) {
        // Находим все записи о дружбе, где участвует текущий пользователь
        List<UserFriend> friendships = userFriendRepository.findAllFriendsForUser(currentUserId); // Предполагаем, что такой метод есть или будет добавлен

        // Преобразуем список сущностей UserFriend в список UserFriendDto
        return friendships.stream().map(friendship -> {
            User friendUser;
            // Определяем, кто из пары является другом (не текущим пользователем)
            if (friendship.getUser1().getId().equals(currentUserId)) {
                friendUser = friendship.getUser2();
            } else {
                friendUser = friendship.getUser1();
            }
            return new UserFriendDto(friendUser.getId(), friendUser.getUsername(), friendUser.getFriendCode());
        }).collect(Collectors.toList());
    }

    @Transactional
    public void removeFriendByCode(Long currentUserId, String friendCodeToRemove) {
        // 1. Найти пользователя, которого хотят удалить, по его friendCode
        User userToRemove = userRepository.findByFriendCode(friendCodeToRemove)
                .orElseThrow(() -> new RuntimeException("User with friend code '" + friendCodeToRemove + "' not found."));

        Long friendIdToRemoveActual = userToRemove.getId(); // Получаем ID пользователя, которого удаляем

        // 2. Проверяем, не пытается ли пользователь удалить сам себя (по ID, на всякий случай)
        if (currentUserId.equals(friendIdToRemoveActual)) {
            throw new RuntimeException("You cannot remove yourself as a friend.");
        }

        // 3. Формируем UserFriendId. Порядок ID важен.
        UserFriendId friendshipId;
        if (currentUserId < friendIdToRemoveActual) {
            friendshipId = new UserFriendId(currentUserId, friendIdToRemoveActual);
        } else {
            friendshipId = new UserFriendId(friendIdToRemoveActual, currentUserId);
        }

        // 4. Проверяем, существует ли такая дружба
        if (!userFriendRepository.existsById(friendshipId)) {
            throw new RuntimeException("You are not friends with the user identified by friend code '" + friendCodeToRemove + "'.");
        }

        // 5. Удаляем запись о дружбе
        userFriendRepository.deleteById(friendshipId);

        System.out.println("User " + currentUserId + " removed friend with code " + friendCodeToRemove + " (User ID: " + friendIdToRemoveActual + ")");
    }
}



