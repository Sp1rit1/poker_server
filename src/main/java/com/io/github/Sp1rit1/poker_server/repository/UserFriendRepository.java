package com.io.github.Sp1rit1.poker_server.repository;

import com.io.github.Sp1rit1.poker_server.entity.UserFriend;
import com.io.github.Sp1rit1.poker_server.entity.UserFriendId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFriendRepository extends JpaRepository<UserFriend, UserFriendId> {

    // Найти все дружеские связи для конкретного пользователя (он может быть user1 или user2)
    @Query("SELECT uf FROM UserFriend uf WHERE uf.id.userId1 = :userId OR uf.id.userId2 = :userId")
    List<UserFriend> findAllFriendsForUser(@Param("userId") Long userId);

    // Проверить, являются ли два пользователя друзьями
    // Учитываем, что пара (A,B) эквивалентна (B,A)
    @Query("SELECT CASE WHEN COUNT(uf) > 0 THEN TRUE ELSE FALSE END " +
            "FROM UserFriend uf " +
            "WHERE (uf.id.userId1 = :userId1 AND uf.id.userId2 = :userId2) OR " +
            "(uf.id.userId1 = :userId2 AND uf.id.userId2 = :userId1)")
    boolean areFriends(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}