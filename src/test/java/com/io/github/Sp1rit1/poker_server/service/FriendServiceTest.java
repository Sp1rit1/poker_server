package com.io.github.Sp1rit1.poker_server.service;

import com.io.github.Sp1rit1.poker_server.dto.UserFriendDto;
import com.io.github.Sp1rit1.poker_server.entity.User;
import com.io.github.Sp1rit1.poker_server.entity.UserFriend;
import com.io.github.Sp1rit1.poker_server.entity.UserFriendId;
import com.io.github.Sp1rit1.poker_server.repository.UserFriendRepository;
import com.io.github.Sp1rit1.poker_server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFriendRepository userFriendRepository;

    @InjectMocks
    private FriendService friendService;

    private User currentUser;
    private User friendUser1;
    private User friendUser2;

    private final Long currentUserId = 1L;
    private final String currentUserFriendCode = "USER1FC";
    private final Long friend1Id = 2L;
    private final String friend1FriendCode = "FRIEND1FC";
    private final Long friend2Id = 3L;
    private final String friend2FriendCode = "FRIEND2FC";

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setUsername("currentUser");
        currentUser.setFriendCode(currentUserFriendCode);

        friendUser1 = new User();
        friendUser1.setId(friend1Id);
        friendUser1.setUsername("friend1");
        friendUser1.setFriendCode(friend1FriendCode);

        friendUser2 = new User();
        friendUser2.setId(friend2Id);
        friendUser2.setUsername("friend2");
        friendUser2.setFriendCode(friend2FriendCode);
    }

    // --- Тесты для addFriend ---

    @Test
    void addFriend_shouldAddFriendSuccessfully_currentUserHasSmallerId() {
        // Arrange
        when(userRepository.findByFriendCode(friend1FriendCode)).thenReturn(Optional.of(friendUser1));
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));

        UserFriendId expectedFriendshipId = new UserFriendId(currentUserId, friend1Id); // currentUser.id < friendUser1.id
        when(userFriendRepository.existsById(expectedFriendshipId)).thenReturn(false);

        // Act
        assertDoesNotThrow(() -> friendService.addFriend(currentUserId, friend1FriendCode));

        // Assert
        ArgumentCaptor<UserFriend> userFriendArgumentCaptor = ArgumentCaptor.forClass(UserFriend.class);
        verify(userFriendRepository, times(1)).save(userFriendArgumentCaptor.capture());
        UserFriend savedFriendship = userFriendArgumentCaptor.getValue();

        assertEquals(expectedFriendshipId, savedFriendship.getId());
        assertEquals(currentUser, savedFriendship.getUser1());
        assertEquals(friendUser1, savedFriendship.getUser2());
        assertEquals("ACCEPTED", savedFriendship.getStatus());
    }


    @Test
    void addFriend_whenUserToAddNotFound_shouldThrowRuntimeException() {
        // Arrange
        when(userRepository.findByFriendCode("NONEXISTENTFC")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> friendService.addFriend(currentUserId, "NONEXISTENTFC"));
        assertEquals("User with friend code 'NONEXISTENTFC' not found.", exception.getMessage());
        verify(userFriendRepository, never()).save(any());
    }

    @Test
    void addFriend_whenAddingSelf_shouldThrowRuntimeException() {
        // Arrange
        when(userRepository.findByFriendCode(currentUserFriendCode)).thenReturn(Optional.of(currentUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> friendService.addFriend(currentUserId, currentUserFriendCode));
        assertEquals("You cannot add yourself as a friend.", exception.getMessage());
        verify(userFriendRepository, never()).save(any());
    }

    @Test
    void addFriend_whenCurrentUserNotFound_shouldThrowRuntimeException() {
        // Arrange
        when(userRepository.findByFriendCode(friend1FriendCode)).thenReturn(Optional.of(friendUser1));
        when(userRepository.findById(currentUserId)).thenReturn(Optional.empty()); // currentUser не найден

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> friendService.addFriend(currentUserId, friend1FriendCode));
        assertEquals("Current user not found. This should not happen.", exception.getMessage());
        verify(userFriendRepository, never()).save(any());
    }

    @Test
    void addFriend_whenAlreadyFriends_idOrder1_shouldThrowRuntimeException() {
        // Arrange
        when(userRepository.findByFriendCode(friend1FriendCode)).thenReturn(Optional.of(friendUser1));
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        UserFriendId id1 = new UserFriendId(currentUserId, friend1Id);
        when(userFriendRepository.existsById(id1)).thenReturn(true); // Симулируем, что они уже друзья (currentUser.id < friendUser1.id)

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> friendService.addFriend(currentUserId, friend1FriendCode));
        assertEquals("You are already friends with this user.", exception.getMessage());
        verify(userFriendRepository, never()).save(any());
    }

    @Test
    void addFriend_whenAlreadyFriends_idOrder2_shouldThrowRuntimeException() {
        // Arrange
        when(userRepository.findByFriendCode(friend1FriendCode)).thenReturn(Optional.of(friendUser1));
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        UserFriendId id1 = new UserFriendId(currentUserId, friend1Id); // Проверяется первым
        UserFriendId id2 = new UserFriendId(friend1Id, currentUserId); // Проверяется вторым
        when(userFriendRepository.existsById(id1)).thenReturn(false);
        when(userFriendRepository.existsById(id2)).thenReturn(true); // Симулируем, что они уже друзья (friendUser1.id < currentUser.id)

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> friendService.addFriend(currentUserId, friend1FriendCode));
        assertEquals("You are already friends with this user.", exception.getMessage());
        verify(userFriendRepository, never()).save(any());
    }

    // --- Тесты для getFriends ---

    @Test
    void getFriends_shouldReturnListOfFriendDtos() {
        // Arrange
        UserFriendId friendship1Id = new UserFriendId(currentUserId, friend1Id);
        UserFriend friendship1 = new UserFriend(friendship1Id, currentUser, friendUser1, "ACCEPTED", null);

        UserFriendId friendship2Id = new UserFriendId(currentUserId, friend2Id);
        UserFriend friendship2 = new UserFriend(friendship2Id, currentUser, friendUser2, "ACCEPTED", null);

        when(userFriendRepository.findAllFriendsForUser(currentUserId)).thenReturn(List.of(friendship1, friendship2));

        // Act
        List<UserFriendDto> friends = friendService.getFriends(currentUserId);

        // Assert
        assertNotNull(friends);
        assertEquals(2, friends.size());

        assertTrue(friends.stream().anyMatch(dto -> dto.getFriendCode().equals(friend1FriendCode) && dto.getUsername().equals("friend1")));
        assertTrue(friends.stream().anyMatch(dto -> dto.getFriendCode().equals(friend2FriendCode) && dto.getUsername().equals("friend2")));
    }

    @Test
    void getFriends_whenFriendshipIsOtherWayAround_shouldReturnCorrectFriendDto() {
        // Arrange
        // Дружба, где currentUserId является user2 в UserFriend
        UserFriendId friendshipId = new UserFriendId(friend1Id, currentUserId); // friend1 (id=2) < currentUser (id=1) - если бы ID были такими
        // Ваша логика упорядочивает ID, так что user1 всегда будет с меньшим ID.
        // Для этого теста важно, чтобы friendUser был правильно определен.
        UserFriend friendship = new UserFriend(friendshipId, friendUser1, currentUser, "ACCEPTED", null);

        when(userFriendRepository.findAllFriendsForUser(currentUserId)).thenReturn(List.of(friendship));

        // Act
        List<UserFriendDto> friends = friendService.getFriends(currentUserId);

        // Assert
        assertNotNull(friends);
        assertEquals(1, friends.size());
        assertEquals(friend1FriendCode, friends.get(0).getFriendCode());
        assertEquals("friend1", friends.get(0).getUsername());
        assertEquals(friend1Id, friends.get(0).getUserId());
    }


    @Test
    void getFriends_whenNoFriends_shouldReturnEmptyList() {
        // Arrange
        when(userFriendRepository.findAllFriendsForUser(currentUserId)).thenReturn(Collections.emptyList());

        // Act
        List<UserFriendDto> friends = friendService.getFriends(currentUserId);

        // Assert
        assertNotNull(friends);
        assertTrue(friends.isEmpty());
    }

    // --- Тесты для removeFriendByCode ---

    @Test
    void removeFriendByCode_shouldRemoveFriendSuccessfully_currentUserHasSmallerId() {
        // Arrange
        when(userRepository.findByFriendCode(friend1FriendCode)).thenReturn(Optional.of(friendUser1));
        UserFriendId expectedFriendshipId = new UserFriendId(currentUserId, friend1Id);
        when(userFriendRepository.existsById(expectedFriendshipId)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> friendService.removeFriendByCode(currentUserId, friend1FriendCode));

        // Assert
        verify(userFriendRepository, times(1)).deleteById(expectedFriendshipId);
    }

    @Test
    void removeFriendByCode_shouldRemoveFriendSuccessfully_currentUserHasLargerId() {
        // Arrange
        // friendUser1 (ID=2) теперь будет иметь меньший ID, чем currentUser (ID=1)
        // Это нелогично для текущих значений, но тест проверяет логику формирования ID
        when(userRepository.findByFriendCode(friend1FriendCode)).thenReturn(Optional.of(
                new User(
                        friend1Id,
                        "friend1",
                        "someHash",
                        "friend1@example.com",
                        BigDecimal.ZERO,
                        friend1FriendCode,
                        null,
                        null
                )
        ));

        Long tempCurrentUserId = 3L; // Делаем currentUserId > friend1Id

        UserFriendId expectedFriendshipId = new UserFriendId(friend1Id, tempCurrentUserId); // (меньший, больший)
        when(userFriendRepository.existsById(expectedFriendshipId)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> friendService.removeFriendByCode(tempCurrentUserId, friend1FriendCode));

        // Assert
        verify(userFriendRepository, times(1)).deleteById(expectedFriendshipId);
    }

    @Test
    void removeFriendByCode_whenUserToRemoveNotFound_shouldThrowRuntimeException() {
        // Arrange
        when(userRepository.findByFriendCode("NONEXISTENTFC")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> friendService.removeFriendByCode(currentUserId, "NONEXISTENTFC"));
        assertEquals("User with friend code 'NONEXISTENTFC' not found.", exception.getMessage());
        verify(userFriendRepository, never()).deleteById(any());
    }

    @Test
    void removeFriendByCode_whenRemovingSelf_shouldThrowRuntimeException() {
        // Arrange
        when(userRepository.findByFriendCode(currentUserFriendCode)).thenReturn(Optional.of(currentUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> friendService.removeFriendByCode(currentUserId, currentUserFriendCode));
        assertEquals("You cannot remove yourself as a friend.", exception.getMessage());
        verify(userFriendRepository, never()).deleteById(any());
    }

    @Test
    void removeFriendByCode_whenNotFriends_shouldThrowRuntimeException() {
        // Arrange
        when(userRepository.findByFriendCode(friend1FriendCode)).thenReturn(Optional.of(friendUser1));
        UserFriendId friendshipId = new UserFriendId(Math.min(currentUserId, friend1Id), Math.max(currentUserId, friend1Id));
        when(userFriendRepository.existsById(friendshipId)).thenReturn(false); // Симулируем, что они не друзья

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> friendService.removeFriendByCode(currentUserId, friend1FriendCode));
        assertEquals("You are not friends with the user identified by friend code '" + friend1FriendCode + "'.", exception.getMessage());
        verify(userFriendRepository, never()).deleteById(any());
    }
}