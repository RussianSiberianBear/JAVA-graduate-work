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

    @Test
    void login_Success_Test() throws Exception {
        Login loginRequest = new Login("user@mail.com", "password123");

        when(authService.login(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void login_Unauthorized_Test() throws Exception {
        Login loginRequest = new Login("user@mail.com", "wrongPassword");

        when(authService.login(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_InvalidEmail_Test() throws Exception {
        Login loginRequest = new Login("invalid-email", "password123");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_EmptyUsername_Test() throws Exception {
        Login loginRequest = new Login("", "password123");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_EmptyPassword_Test() throws Exception {
        Login loginRequest = new Login("user@mail.com", "");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_PasswordTooShort_Test() throws Exception {
        Login loginRequest = new Login("user@mail.com", "123");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    // ===== ТЕСТЫ ДЛЯ REGISTER =====
    @Test
    void register_Success_Test() throws Exception {
        Register registerRequest = new Register(
                "user@mail.com",
                "Password123@",
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
                "Password123!",
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
                "Password123!",
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
                "Password123@",
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

    // ===== ТЕСТЫ НА ПУСТЫЕ И NULL ЗНАЧЕНИЯ С ПРОВЕРКОЙ СООБЩЕНИЙ =====

    @Test
    void register_EmptyUsername_ThrowsException_Test() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Register("", "Password123!", "John", "Doe", "+79991234567", Role.USER)
        );
        assertEquals("Логин пользователя не может быть пустым!", exception.getMessage());
    }

    @Test
    void register_EmptyPassword_ThrowsException_Test() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Register("user@mail.com", "", "John", "Doe", "+79991234567", Role.USER)
        );
        assertEquals("Пароль не может быть пустым!", exception.getMessage());
    }

    @Test
    void register_EmptyFirstName_ThrowsException_Test() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Register("user@mail.com", "Password123!", "", "Doe", "+79991234567", Role.USER)
        );
        assertEquals("Имя пользователя не может быть пустым!", exception.getMessage());
    }

    @Test
    void register_EmptyLastName_ThrowsException_Test() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Register("user@mail.com", "Password123!", "John", "", "+79991234567", Role.USER)
        );
        assertEquals("Фамилия пользователя не может быть пустым!", exception.getMessage());
    }

    @Test
    void register_EmptyPhone_ThrowsException_Test() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Register("user@mail.com", "Password123!", "John", "Doe", "", Role.USER)
        );
        assertEquals("Телефон пользователя не может быть пустым!", exception.getMessage());
    }

    @Test
    void register_NullRole_ThrowsException_Test() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Register("user@mail.com", "Password123!", "John", "Doe", "+79991234567", null)
        );
        assertEquals("Роль пользователя не может быть пустым значением!", exception.getMessage());
    }

    @Test
    void register_NullUsername_ThrowsException_Test() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Register(null, "Password123!", "John", "Doe", "+79991234567", Role.USER)
        );
        assertEquals("Логин пользователя не может быть пустым!", exception.getMessage());
    }

    @Test
    void register_NullPassword_ThrowsException_Test() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Register("user@mail.com", null, "John", "Doe", "+79991234567", Role.USER)
        );
        assertEquals("Пароль не может быть пустым!", exception.getMessage());
    }

    @Test
    void register_NullFirstName_ThrowsException_Test() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Register("user@mail.com", "Password123!", null, "Doe", "+79991234567", Role.USER)
        );
        assertEquals("Имя пользователя не может быть пустым!", exception.getMessage());
    }

    @Test
    void register_NullLastName_ThrowsException_Test() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Register("user@mail.com", "Password123!", "John", null, "+79991234567", Role.USER)
        );
        assertEquals("Фамилия пользователя не может быть пустым!", exception.getMessage());
    }

    @Test
    void register_NullPhone_ThrowsException_Test() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Register("user@mail.com", "Password123!", "John", "Doe", null, Role.USER)
        );
        assertEquals("Телефон пользователя не может быть пустым!", exception.getMessage());
    }

    @Test
    void register_FirstNameWithNumbers_Test() throws Exception {
        Register registerRequest = new Register(
                "user@mail.com",
                "Password123!",
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
                "Password123!",
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