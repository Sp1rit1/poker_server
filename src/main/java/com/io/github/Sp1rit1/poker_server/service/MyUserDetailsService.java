package com.io.github.Sp1rit1.poker_server.service;

import com.io.github.Sp1rit1.poker_server.entity.User;
import com.io.github.Sp1rit1.poker_server.repository.UserRepository;
import com.io.github.Sp1rit1.poker_server.security.CustomUserDetails; // Импортируем наш новый класс
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections; // Для пустого списка прав
// import java.util.stream.Collectors; // Если у вас есть роли

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // Важно для LAZY загрузки, если есть связанные сущности (например, роли)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Если у вас есть система ролей, здесь вы бы их загрузили и преобразовали в GrantedAuthority
        // Пример с пустым списком прав (если роли не используются):
        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

        /* Пример, если бы у User entity был список ролей (List<Role> roles):
        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase())) // Префикс ROLE_ - стандарт
                .collect(Collectors.toList());
        */

        // Создаем и возвращаем CustomUserDetails
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getFriendCode(), // Передаем friendCode
                authorities,
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true  // accountNonLocked
        );
    }
}
