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
@Table(name = "user_friends")
public class UserFriend {

    @EmbeddedId // Указывает, что ID этой сущности является встроенным классом (UserFriendId)
    private UserFriendId id;

    @ManyToOne(fetch = FetchType.LAZY) // Отношение "многие-к-одному" с User
    @MapsId("userId1") // Сопоставляет поле userId1 из UserFriendId с внешним ключом
    @JoinColumn(name = "user_id1", referencedColumnName = "id")
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId2")
    @JoinColumn(name = "user_id2", referencedColumnName = "id")
    private User user2;

    @Column(nullable = false, length = 10)
    private String status; // Например, "ACCEPTED", "PENDING"

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "ACCEPTED"; // Статус по умолчанию, если не задан
        }
    }
}