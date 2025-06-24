package com.io.github.Sp1rit1.poker_server.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable // Указывает, что этот класс может быть встроен в другую сущность как часть ID
public class UserFriendId implements Serializable {

    private static final long serialVersionUID = 1L; // Для Serializable

    private Long userId1;
    private Long userId2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFriendId that = (UserFriendId) o;
        return Objects.equals(userId1, that.userId1) &&
                Objects.equals(userId2, that.userId2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId1, userId2);
    }
}