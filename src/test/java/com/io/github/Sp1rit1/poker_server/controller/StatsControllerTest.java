package com.io.github.Sp1rit1.poker_server.controller; // Убедитесь, что пакет правильный

// ObjectMapper не нужен для @Autowired, если мы не создаем JSON запросы вручную здесь.
// import com.fasterxml.jackson.databind.ObjectMapper;
import com.io.github.Sp1rit1.poker_server.config.SecurityConfig;
import com.io.github.Sp1rit1.poker_server.dto.UserStatsDto;
import com.io.github.Sp1rit1.poker_server.security.CustomUserDetails;
import com.io.github.Sp1rit1.poker_server.security.jwt.JwtTokenProvider;
import com.io.github.Sp1rit1.poker_server.service.MyUserDetailsService;
import com.io.github.Sp1rit1.poker_server.service.StatsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito; // Для Mockito.mock()
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration; // Для конфигурации моков как бинов
import org.springframework.context.annotation.Bean; // Для @Bean
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

// Статические импорты
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;


@WebMvcTest(StatsController.class)
@Import({SecurityConfig.class, StatsControllerTest.TestConfig.class}) // Импортируем SecurityConfig и нашу TestConfig
class StatsControllerTest {

    // Вложенная конфигурация для создания моков как бинов
    @TestConfiguration
    static class TestConfig {
        @Bean
        public StatsService statsService() {
            return Mockito.mock(StatsService.class);
        }

        @Bean // Мокируем UserDetailsService, так как он требуется SecurityConfig
        public MyUserDetailsService myUserDetailsService() {
            return Mockito.mock(MyUserDetailsService.class);
        }

        @Bean // Мокируем JwtTokenProvider, так как он может требоваться SecurityConfig
        public JwtTokenProvider jwtTokenProvider() {
            return Mockito.mock(JwtTokenProvider.class);
        }
        // AuthenticationManager обычно предоставляется Spring Security,
        // если он не объявлен как @Bean в SecurityConfig, то Spring его сам настроит
        // или если он вам нужен как мок для других тестов, можно добавить @Bean AuthenticationManager.
    }


    @Autowired
    private MockMvc mockMvc;

    @Autowired // Теперь Spring внедрит мок StatsService из TestConfig
    private StatsService statsService;

    // ObjectMapper может быть @Autowired, если он вам нужен для создания JSON тел запросов вручную,
    // но для простых GET и проверки JSON ответов он часто не нужен явно в полях теста.
    // @Autowired
    // private ObjectMapper objectMapper;

    private UserStatsDto userStatsDto;
    private CustomUserDetails userDetails;
    private Long testUserId = 1L;
    private String testUsername = "testuser";
    private String testFriendCode = "TESTFC";


    @BeforeEach
    void setUp() {
        userStatsDto = new UserStatsDto();
        userStatsDto.setUserId(testUserId);
        userStatsDto.setHandsPlayed(100);
        userStatsDto.setHandsWon(50);

        userDetails = new CustomUserDetails(
                testUserId,
                testUsername,
                "password",
                testFriendCode,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                true, true, true, true
        );
    }

    private Authentication getMockAuthentication() {
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    void getCurrentPlayerStats_whenUserAuthenticated_shouldReturnStatsDto() throws Exception {
        // Arrange
        Authentication authentication = getMockAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        given(statsService.getPlayerStats(testUserId)).willReturn(userStatsDto);

        // Act & Assert
        mockMvc.perform(get("/api/stats/me")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", is(testUserId.intValue())))
                .andExpect(jsonPath("$.handsPlayed", is(100)))
                .andExpect(jsonPath("$.handsWon", is(50)));

        verify(statsService).getPlayerStats(testUserId);
        SecurityContextHolder.clearContext();
    }


    @Test
    void getCurrentPlayerStats_whenPrincipalIsNotCustomUserDetails_shouldReturnUnauthorized() throws Exception {
        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User("standarduser", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication standardAuthentication = new UsernamePasswordAuthenticationToken(springUser, null, springUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(standardAuthentication);

        mockMvc.perform(get("/api/stats/me")
                        .principal(standardAuthentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("User not authenticated properly.")));

        verify(statsService, never()).getPlayerStats(anyLong());
        SecurityContextHolder.clearContext();
    }
}