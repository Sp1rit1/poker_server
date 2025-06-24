package com.io.github.Sp1rit1.poker_server.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data

public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username; // final
    @JsonIgnore
    private final String password; // final
    private final String friendCode; // final
    private final Collection<? extends GrantedAuthority> authorities; // final
    private final boolean accountNonExpired; // final
    private final boolean accountNonLocked; // final
    private final boolean credentialsNonExpired; // final
    private final boolean enabled; // final

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