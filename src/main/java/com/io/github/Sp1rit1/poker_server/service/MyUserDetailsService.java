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

        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

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
