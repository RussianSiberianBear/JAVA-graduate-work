package ru.skypro.homework.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.homework.dto.Login;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.service.AuthService;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    // ===== ТЕСТЫ ДЛЯ LOGIN =====

    // Валидные данные для Login DTO (проходят @Pattern валидацию)
    private static final String VALID_LOGIN_USERNAME = "user@mail.com";
    private static final String VALID_LOGIN_PASSWORD = "Password123@";

    @Test
    void login_Success_Test() throws Exception {
        Login loginRequest = new Login(VALID_LOGIN_USERNAME, VALID_LOGIN_PASSWORD);

        when(authService.login(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void login_Unauthorized_Test() throws Exception {
        Login loginRequest = new Login(VALID_LOGIN_USERNAME, VALID_LOGIN_PASSWORD);

        when(authService.login(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_InvalidEmail_Test() throws Exception {
        Login loginRequest = new Login("invalid-email", VALID_LOGIN_PASSWORD);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_EmptyUsername_Test() throws Exception {
        Login loginRequest = new Login("", VALID_LOGIN_PASSWORD);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_EmptyPassword_Test() throws Exception {
        Login loginRequest = new Login(VALID_LOGIN_USERNAME, "");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_PasswordTooShort_Test() throws Exception {
        Login loginRequest = new Login(VALID_LOGIN_USERNAME, "123");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    // Валидные данные для Register DTO (проходят @Pattern валидацию)
    private static final String VALID_REGISTER_PASSWORD = "Password123@";

    // ===== ТЕСТЫ ДЛЯ REGISTER =====
    @Test
    void register_Success_Test() throws Exception {
        Register registerRequest = new Register(
                "user@mail.com",
                VALID_REGISTER_PASSWORD,
                "John",
                "Doe",
                "+79991234567",
                Role.USER
        );

        when(authService.register(any(Register.class))).thenReturn(true);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void register_BadRequest_WhenUserExists_Test() throws Exception {
        Register registerRequest = new Register(
                "existing@mail.com",
                VALID_REGISTER_PASSWORD,
                "John",
                "Doe",
                "+79991234567",
                Role.USER
        );

        when(authService.register(any(Register.class))).thenReturn(false);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_InvalidEmail_Test() throws Exception {
        Register registerRequest = new Register(
                "invalid-email",
                VALID_REGISTER_PASSWORD,
                "John",
                "Doe",
                "+79991234567",
                Role.USER
        );

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_PasswordTooShort_Test() throws Exception {
        Register registerRequest = new Register(
                "user@mail.com",
                "123",
                "John",
                "Doe",
                "+79991234567",
                Role.USER
        );

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_PasswordWithoutSpecialChar_Test() throws Exception {
        Register registerRequest = new Register(
                "user@mail.com",
                "Password123",
                "John",
                "Doe",
                "+79991234567",
                Role.USER
        );

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_AdminRole_Success_Test() throws Exception {
        Register registerRequest = new Register(
                "admin@mail.com",
                VALID_REGISTER_PASSWORD,
                "Admin",
                "Adminov",
                "+79991234567",
                Role.ADMIN
        );

        when(authService.register(registerRequest)).thenReturn(true);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    // Примечание: валидация полей (NotBlank, NotNull, Size и т.д.)
    // проверяется через MockMvc выше (статус 400 при невалидных данных).
    // Тесты, создающие DTO напрямую и ожидающие IllegalArgumentException,
    // не работают, так как Jakarta Validation срабатывает при вызове
    // Validator.validate(), а не при создании record.

    @Test
    void register_FirstNameWithNumbers_Test() throws Exception {
        Register registerRequest = new Register(
                "user@mail.com",
                VALID_REGISTER_PASSWORD,
                "John123",
                "Doe",
                "+79991234567",
                Role.USER
        );

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_InvalidPhone_Test() throws Exception {
        Register registerRequest = new Register(
                "user@mail.com",
                VALID_REGISTER_PASSWORD,
                "John",
                "Doe",
                "123",
                Role.USER
        );

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

}