package com.io.github.Sp1rit1.poker_server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.io.github.Sp1rit1.poker_server.config.SecurityConfig;
import com.io.github.Sp1rit1.poker_server.dto.AuthResponseDto;
import com.io.github.Sp1rit1.poker_server.dto.LoginRequestDto;
import com.io.github.Sp1rit1.poker_server.dto.UserRegistrationDto;
import com.io.github.Sp1rit1.poker_server.entity.User;
import com.io.github.Sp1rit1.poker_server.security.CustomUserDetails;
import com.io.github.Sp1rit1.poker_server.security.jwt.JwtTokenProvider;
import com.io.github.Sp1rit1.poker_server.service.MyUserDetailsService;
import com.io.github.Sp1rit1.poker_server.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito; // Для Mockito.mock()
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;

// Статические импорты
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;


@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuthControllerTest.TestConfig.class}) // Импортируем SecurityConfig и нашу TestConfig
class AuthControllerTest {

    // Вложенная конфигурация для создания моков как бинов
    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthenticationManager authenticationManager() {
            return Mockito.mock(AuthenticationManager.class);
        }

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public JwtTokenProvider jwtTokenProvider() {
            return Mockito.mock(JwtTokenProvider.class);
        }

        @Bean // Мокируем UserDetailsService, так как он требуется SecurityConfig
        public MyUserDetailsService myUserDetailsService() {
            return Mockito.mock(MyUserDetailsService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    // Теперь эти зависимости будут внедрены Spring из нашей TestConfig
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationDto registrationDto;
    private LoginRequestDto loginRequestDto;
    private User registeredUserStub;
    private CustomUserDetails userDetails;
    private final String MOCK_JWT_TOKEN = "mock.jwt.token.string";
    private final Long TEST_USER_ID = 1L;
    private final String TEST_USERNAME = "testuser";
    private final String TEST_FRIEND_CODE = "TESTFC123";


    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setUsername(TEST_USERNAME);
        registrationDto.setPassword("password123");
        registrationDto.setEmail("test@example.com");

        loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUsername(TEST_USERNAME);
        loginRequestDto.setPassword("password123");

        registeredUserStub = new User();
        registeredUserStub.setId(TEST_USER_ID);
        registeredUserStub.setUsername(TEST_USERNAME);
        registeredUserStub.setFriendCode(TEST_FRIEND_CODE);

        userDetails = new CustomUserDetails(
                TEST_USER_ID,
                TEST_USERNAME,
                "hashedPassword",
                TEST_FRIEND_CODE,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                true, true, true, true
        );
    }

    // --- Тесты для /register ---

    @Test
    void registerUser_whenValidDto_shouldReturnCreatedAndSuccessMessage() throws Exception {
        // Экспериментальный сброс мока перед настройкой
        Mockito.reset(userService); // Добавьте это временно

        // Arrange
        given(userService.registerUser(any(UserRegistrationDto.class))).willReturn(registeredUserStub);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)));

        // Assert
        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("User registered successfully! Please log in.")));

        verify(userService).registerUser(any(UserRegistrationDto.class));
    }

    @Test
    void registerUser_whenUsernameAlreadyExists_shouldReturnBadRequestFromGlobalHandler() throws Exception {
        // Arrange
        String specificErrorMessage = "Username '" + registrationDto.getUsername() + "' already exists";
        given(userService.registerUser(any(UserRegistrationDto.class)))
                .willThrow(new RuntimeException(specificErrorMessage)); // Бросаем RuntimeException с этим сообщением

        // Act
        ResultActions resultActions = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)));

        // Assert
        resultActions.andExpect(status().isBadRequest()) // Проверяем HTTP статус 400
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request"))) // Поле "error" из GlobalExceptionHandler
                .andExpect(jsonPath("$.message", is(specificErrorMessage))) // Поле "message" содержит текст исключения
                .andExpect(jsonPath("$.path", is("/api/auth/register"))) // Проверяем путь
                .andExpect(jsonPath("$.timestamp").exists()); // Проверяем наличие timestamp
    }

    @Test
    void registerUser_whenInvalidDto_shouldReturnBadRequestWithValidationErrors() throws Exception {
        UserRegistrationDto invalidDto = new UserRegistrationDto();
        invalidDto.setUsername("");
        invalidDto.setEmail(null);
        invalidDto.setPassword(null);

        ResultActions resultActions = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }


    // --- Тесты для /login ---

    @Test
    void authenticateUser_whenValidCredentials_shouldReturnOkWithAuthResponseDto() throws Exception {
        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(mockAuthentication);
        given(tokenProvider.generateToken(mockAuthentication)).willReturn(MOCK_JWT_TOKEN);

        ResultActions resultActions = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", is(userDetails.getId().intValue())))
                .andExpect(jsonPath("$.username", is(userDetails.getUsername())))
                .andExpect(jsonPath("$.friendCode", is(userDetails.getFriendCode())))
                .andExpect(jsonPath("$.accessToken", is(MOCK_JWT_TOKEN))); // Проверяем accessToken

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(mockAuthentication);
    }

    @Test
    void authenticateUser_whenInvalidCredentials_shouldBeHandledByGlobalHandlerAndReturnBadRequest() throws Exception {
        // Arrange
        String specificErrorMessageFromException = "Invalid credentials from mock"; // Это сообщение будет в BadCredentialsException

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException(specificErrorMessageFromException));

        // Act
        ResultActions resultActions = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)));

        // Assert
        resultActions.andExpect(status().isBadRequest()) // Ожидаем 400, так как GlobalExceptionHandler обрабатывает RuntimeException
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request"))) // Или тот errorType, который устанавливается для RuntimeException
                .andExpect(jsonPath("$.message", is(specificErrorMessageFromException))) // Проверяем сообщение из исключения
                .andExpect(jsonPath("$.path", is("/api/auth/login")))
                .andExpect(jsonPath("$.timestamp").exists()); // Проверяем наличие timestamp
    }

    @Test
    void authenticateUser_whenInvalidLoginDto_shouldReturnBadRequest() throws Exception {
        LoginRequestDto invalidLoginDto = new LoginRequestDto();
        invalidLoginDto.setUsername("");
        invalidLoginDto.setPassword(null);

        ResultActions resultActions = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLoginDto)));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }
}