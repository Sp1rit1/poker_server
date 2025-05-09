package com.io.github.Sp1rit1.poker_server.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data; // Добавляем аннотацию Lombok
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data // Эта аннотация сгенерирует геттеры для id, username, password, friendCode, authorities и т.д.
// А также сеттеры (если поля не final), equals, hashCode, toString
public class CustomUserDetails implements UserDetails {

    private final Long id; // final - сеттер не будет сгенерирован, только геттер
    private final String username; // final
    @JsonIgnore
    private final String password; // final
    private final String friendCode; // final
    private final Collection<? extends GrantedAuthority> authorities; // final
    private final boolean accountNonExpired; // final
    private final boolean accountNonLocked; // final
    private final boolean credentialsNonExpired; // final
    private final boolean enabled; // final

    // Конструктор для полной инициализации (Lombok @Data не генерирует конструкторы со всеми полями,
    // для этого нужен @AllArgsConstructor, но т.к. все поля final, он и так будет нужен)
    // Если вы хотите, чтобы Lombok сгенерировал этот конструктор, добавьте @AllArgsConstructor
    public CustomUserDetails(Long id, String username, String password, String friendCode,
                             Collection<? extends GrantedAuthority> authorities,
                             boolean enabled, boolean accountNonExpired,
                             boolean credentialsNonExpired, boolean accountNonLocked) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.friendCode = friendCode;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
    }

    // Упрощенный конструктор
    public CustomUserDetails(Long id, String username, String password, String friendCode) {
        this(id, username, password, friendCode, Collections.emptyList(), true, true, true, true);
    }
}