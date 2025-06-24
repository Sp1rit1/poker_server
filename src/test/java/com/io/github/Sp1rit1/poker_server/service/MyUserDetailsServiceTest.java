package com.io.github.Sp1rit1.poker_server.service;

import com.io.github.Sp1rit1.poker_server.entity.User;
import com.io.github.Sp1rit1.poker_server.repository.UserRepository;
import com.io.github.Sp1rit1.poker_server.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyUserDetailsService userDetailsService;

    private User testUser;
    private final String existingUsername = "testuser"; // Исправлено
    private final String nonExistentUsername = "nonexistentuser";
    private final Long testUserId = 1L;
    private final String testPasswordHash = "hashedPassword";
    private final String testFriendCode = "TESTFC";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername(existingUsername); // Исправлено
        testUser.setPasswordHash(testPasswordHash);
        testUser.setEmail("test@example.com");
        testUser.setBalance(BigDecimal.valueOf(1000));
        testUser.setFriendCode(testFriendCode);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUserStats(null);
    }

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnCustomUserDetails() {
        // Arrange
        when(userRepository.findByUsername(existingUsername)).thenReturn(Optional.of(testUser)); // Исправлено

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(existingUsername); // Исправлено

        // Assert
        assertNotNull(userDetails, "UserDetails should not be null");
        assertTrue(userDetails instanceof CustomUserDetails, "UserDetails should be an instance of CustomUserDetails");

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertEquals(testUserId, customUserDetails.getId(), "User ID should match");
        assertEquals(existingUsername, customUserDetails.getUsername(), "Username should match"); // Исправлено
        assertEquals(testPasswordHash, customUserDetails.getPassword(), "Password hash should match");
        assertEquals(testFriendCode, customUserDetails.getFriendCode(), "Friend code should match");

        assertTrue(customUserDetails.isAccountNonExpired(), "Account should be non-expired");
        assertTrue(customUserDetails.isAccountNonLocked(), "Account should be non-locked");
        assertTrue(customUserDetails.isCredentialsNonExpired(), "Credentials should be non-expired");
        assertTrue(customUserDetails.isEnabled(), "Account should be enabled");

        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
        assertNotNull(authorities, "Authorities should not be null");
        assertTrue(authorities.isEmpty(), "Authorities should be empty as no roles are defined");

        verify(userRepository, times(1)).findByUsername(existingUsername); // Исправлено
    }

    @Test
    void loadUserByUsername_whenUserNotFound_shouldThrowUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(nonExistentUsername);
        });

        assertEquals("User not found with username: " + nonExistentUsername, exception.getMessage(), "Exception message should match");
        verify(userRepository, times(1)).findByUsername(nonExistentUsername);
    }

    @Test
    void loadUserByUsername_whenUsernameIsNull_shouldThrowUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(null);
        });
        assertEquals("User not found with username: null", exception.getMessage()); // Сообщение об ошибке также должно быть корректным
        verify(userRepository, times(1)).findByUsername(null);
    }
}