package com.io.github.Sp1rit1.poker_server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.io.github.Sp1rit1.poker_server.config.SecurityConfig;
import com.io.github.Sp1rit1.poker_server.dto.FriendCodeRequestDto;
import com.io.github.Sp1rit1.poker_server.dto.UserFriendDto;
import com.io.github.Sp1rit1.poker_server.entity.User; // Необходим для создания тестовых объектов User
import com.io.github.Sp1rit1.poker_server.entity.UserFriendId; // Необходим для мокирования userFriendRepository
import com.io.github.Sp1rit1.poker_server.repository.UserFriendRepository; // Мокируем эту зависимость FriendService
import com.io.github.Sp1rit1.poker_server.repository.UserRepository; // Мокируем эту зависимость FriendService
import com.io.github.Sp1rit1.poker_server.security.CustomUserDetails;
import com.io.github.Sp1rit1.poker_server.security.jwt.JwtTokenProvider;
import com.io.github.Sp1rit1.poker_server.service.FriendService;
import com.io.github.Sp1rit1.poker_server.service.MyUserDetailsService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;


@WebMvcTest(FriendController.class)
@Import({SecurityConfig.class, FriendControllerTest.TestConfig.class})
class FriendControllerTest {

    @TestConfiguration
    static class TestConfig {
        // Мокируем сервисы, которые использует FriendController
        @Bean
        public FriendService friendService() { return Mockito.mock(FriendService.class); }

        // Мокируем зависимости, необходимые для SecurityConfig
        @Bean
        public MyUserDetailsService myUserDetailsService() { return Mockito.mock(MyUserDetailsService.class); }
        @Bean
        public JwtTokenProvider jwtTokenProvider() { return Mockito.mock(JwtTokenProvider.class); }


    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired // Теперь это мок, предоставленный TestConfig
    private FriendService friendService;




    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails userDetails;
    private final Long currentUserId = 1L;
    private final String currentUserFriendCode = "USER1FC";
    private final String friendCodeToAdd = "FRIEND2FC";
    private FriendCodeRequestDto friendCodeRequestDto;
    private Authentication mockAuthentication;


    @BeforeEach
    void setUp() {
        userDetails = new CustomUserDetails(
                currentUserId, "currentUser", "password", currentUserFriendCode,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                true, true, true, true
        );
        mockAuthentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        friendCodeRequestDto = new FriendCodeRequestDto();
        friendCodeRequestDto.setFriendCode(friendCodeToAdd);

        Mockito.reset(friendService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupAuthenticatedContext() {
        SecurityContextHolder.getContext().setAuthentication(mockAuthentication);
    }


    @Test
    void addFriend_whenInvalidDto_shouldReturnBadRequest() throws Exception {
        setupAuthenticatedContext(); // Аутентификация нужна, чтобы дойти до валидации DTO
        FriendCodeRequestDto invalidDto = new FriendCodeRequestDto();
        invalidDto.setFriendCode(""); // Невалидный код

        ResultActions resultActions = mockMvc.perform(post("/api/friends/add")
                .principal(mockAuthentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.friendCode").exists());
        verify(friendService, never()).addFriend(anyLong(), anyString()); // Сервис не должен быть вызван
    }


    // --- Тесты для GET /api/friends/list ---
    @Test
    void getFriendList_whenAuthenticated_shouldReturnFriendList() throws Exception {
        setupAuthenticatedContext();
        List<UserFriendDto> mockFriends = Arrays.asList(
                new UserFriendDto(2L, "friendOne", "FRIEND1CD"),
                new UserFriendDto(3L, "friendTwo", "FRIEND2CD")
        );
        given(friendService.getFriends(currentUserId)).willReturn(mockFriends);

        ResultActions resultActions = mockMvc.perform(get("/api/friends/list")
                .principal(mockAuthentication)
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("friendOne")))
                .andExpect(jsonPath("$[1].friendCode", is("FRIEND2CD")));
        verify(friendService).getFriends(currentUserId);
    }

    @Test
    void getFriendList_whenAuthenticatedAndNoFriends_shouldReturnEmptyList() throws Exception {
        setupAuthenticatedContext();
        given(friendService.getFriends(currentUserId)).willReturn(Collections.emptyList());

        ResultActions resultActions = mockMvc.perform(get("/api/friends/list")
                .principal(mockAuthentication)
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        verify(friendService).getFriends(currentUserId);
    }


    // --- Тесты для DELETE /api/friends/remove-by-code/{friendCodeToRemove} ---
    @Test
    void removeFriendByCode_whenAuthenticatedAndValidCode_shouldReturnOk() throws Exception {
        setupAuthenticatedContext();
        String friendCodeToRemove = "FRIEND2REMOVE";
        doNothing().when(friendService).removeFriendByCode(eq(currentUserId), eq(friendCodeToRemove));

        ResultActions resultActions = mockMvc.perform(delete("/api/friends/remove-by-code/" + friendCodeToRemove)
                .principal(mockAuthentication)
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Friend removed successfully using friend code.")));
        verify(friendService).removeFriendByCode(currentUserId, friendCodeToRemove);
    }

    @Test
    void removeFriendByCode_whenRemovingSelfByOwnCode_shouldReturnBadRequest() throws Exception {
        setupAuthenticatedContext();
        ResultActions resultActions = mockMvc.perform(delete("/api/friends/remove-by-code/" + currentUserFriendCode)
                .principal(mockAuthentication)
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("You cannot remove yourself using your own friend code.")));
        verify(friendService, never()).removeFriendByCode(anyLong(), anyString());
    }


}