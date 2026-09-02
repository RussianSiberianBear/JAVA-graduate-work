package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    // ===== КОНСТАНТЫ =====
    private static final String USER_EMAIL = "user@mail.com";
    private static final String USER_PASSWORD = "Password123@";
    private static final String ENCODED_PASSWORD = "encodedPassword123@";
    private static final String WRONG_PASSWORD = "wrongPassword";
    private static final String USER_FIRST_NAME = "John";
    private static final String USER_LAST_NAME = "Doe";
    private static final String USER_PHONE = "+79991234567";
    private static final Role USER_ROLE = Role.USER;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthServiceImpl authService;

    // ===== МЕТОДЫ-ФАБРИКИ =====

    private Register createDefaultRegister() {
        return new Register(
                USER_EMAIL,
                USER_PASSWORD,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_PHONE,
                USER_ROLE
        );
    }

    private User createDefaultUser() {
        User user = new User();
        user.setEmail(USER_EMAIL);
        user.setPassword(ENCODED_PASSWORD);
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        user.setPhone(USER_PHONE);
        user.setRole(USER_ROLE);
        return user;
    }

    // ===== ТЕСТЫ ДЛЯ login =====

    @Test
    void login_Success_Test() {
        User user = createDefaultUser();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(encoder.matches(USER_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        boolean result = authService.login(USER_EMAIL, USER_PASSWORD);

        assertThat(result).isTrue();
        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(encoder, times(1)).matches(USER_PASSWORD, ENCODED_PASSWORD);
    }

    @Test
    void login_WithWrongPassword_Test() {
        User user = createDefaultUser();
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(encoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        boolean result = authService.login(USER_EMAIL, WRONG_PASSWORD);

        assertThat(result).isFalse();
        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(encoder, times(1)).matches(WRONG_PASSWORD, ENCODED_PASSWORD);
    }

    @Test
    void login_UserNotFound_Test() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        boolean result = authService.login(USER_EMAIL, USER_PASSWORD);

        assertThat(result).isFalse();
        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(encoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WithEmptyUsername_Test() {
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        boolean result = authService.login("", USER_PASSWORD);

        assertThat(result).isFalse();
        verify(userRepository, times(1)).findByEmail("");
        verify(encoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WithNullUsername_Test() {
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        boolean result = authService.login(null, USER_PASSWORD);

        assertThat(result).isFalse();
        verify(userRepository, times(1)).findByEmail(null);
        verify(encoder, never()).matches(anyString(), anyString());
    }

    // ===== ТЕСТЫ ДЛЯ register =====

    @Test
    void register_Success_Test() {
        Register register = createDefaultRegister();
        User user = createDefaultUser();

        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(false);
        when(userMapper.toEntity(register)).thenReturn(user);
        when(encoder.encode(USER_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(user)).thenReturn(user);

        boolean result = authService.register(register);

        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsByEmail(USER_EMAIL);
        verify(userMapper, times(1)).toEntity(register);
        verify(encoder, times(1)).encode(USER_PASSWORD);
        verify(userRepository, times(1)).save(user);
        assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
    }

    @Test
    void register_UserAlreadyExists_Test() {
        Register register = createDefaultRegister();

        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(true);

        boolean result = authService.register(register);

        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsByEmail(USER_EMAIL);
        verify(userMapper, never()).toEntity(any());
        verify(encoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WithAdminRole_Success_Test() {
        Register register = new Register(
                "admin@mail.com",
                "Admin123@",
                "Admin",
                "Adminov",
                "+79998887766",
                Role.ADMIN
        );

        User user = new User();
        user.setEmail("admin@mail.com");
        user.setPassword(ENCODED_PASSWORD);
        user.setFirstName("Admin");
        user.setLastName("Adminov");
        user.setPhone("+79998887766");
        user.setRole(Role.ADMIN);

        when(userRepository.existsByEmail("admin@mail.com")).thenReturn(false);
        when(userMapper.toEntity(register)).thenReturn(user);
        when(encoder.encode("Admin123@")).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(user)).thenReturn(user);

        boolean result = authService.register(register);

        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsByEmail("admin@mail.com");
        verify(userMapper, times(1)).toEntity(register);
        verify(encoder, times(1)).encode("Admin123@");
        verify(userRepository, times(1)).save(user);
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    // ===== ТЕСТЫ НА ИСКЛЮЧЕНИЯ =====

    @Test
    void register_WhenUserMapperReturnsNull_ThrowsNPE_Test() {
        Register register = createDefaultRegister();

        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(false);
        when(userMapper.toEntity(register)).thenReturn(null);

        // Проверяем, что выбрасывается NPE при попытке вызвать setPassword(null)
        assertThatThrownBy(() -> authService.register(register))
                .isInstanceOf(NullPointerException.class);

        verify(userRepository, times(1)).existsByEmail(USER_EMAIL);
        verify(userMapper, times(1)).toEntity(register);
        // encoder.encode вызывается до setPassword, поэтому он вызывается
        verify(encoder, times(1)).encode(USER_PASSWORD);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WhenUserRepositoryThrowsException_Test() {
        Register register = createDefaultRegister();
        User user = createDefaultUser();

        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(false);
        when(userMapper.toEntity(register)).thenReturn(user);
        when(encoder.encode(USER_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(user)).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> authService.register(register))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(userRepository, times(1)).existsByEmail(USER_EMAIL);
        verify(userMapper, times(1)).toEntity(register);
        verify(encoder, times(1)).encode(USER_PASSWORD);
        verify(userRepository, times(1)).save(user);
    }

    // Примечание: валидация полей (NotBlank, NotNull, Size и т.д.)
    // проверяется на уровне контроллера через MockMvc (AuthControllerTest),
    // так как Jakarta Validation срабатывает при вызове Validator.validate(),
    // а не при создании record.
}
