package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private UserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private UserDetails userDetails;

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
        when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(userDetails);
        when(userDetails.getPassword()).thenReturn(ENCODED_PASSWORD);
        when(encoder.matches(USER_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        boolean result = authService.login(USER_EMAIL, USER_PASSWORD);

        assertThat(result).isTrue();
        verify(userDetailsService, times(1)).loadUserByUsername(USER_EMAIL);
        verify(encoder, times(1)).matches(USER_PASSWORD, ENCODED_PASSWORD);
    }

    @Test
    void login_WithWrongPassword_Test() {
        when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(userDetails);
        when(userDetails.getPassword()).thenReturn(ENCODED_PASSWORD);
        when(encoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        boolean result = authService.login(USER_EMAIL, WRONG_PASSWORD);

        assertThat(result).isFalse();
        verify(userDetailsService, times(1)).loadUserByUsername(USER_EMAIL);
        verify(encoder, times(1)).matches(WRONG_PASSWORD, ENCODED_PASSWORD);
    }

    @Test
    void login_UserNotFound_Test() {
        when(userDetailsService.loadUserByUsername(USER_EMAIL))
                .thenThrow(new UsernameNotFoundException("User not found"));

        boolean result = authService.login(USER_EMAIL, USER_PASSWORD);

        assertThat(result).isFalse();
        verify(userDetailsService, times(1)).loadUserByUsername(USER_EMAIL);
        verify(encoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WithEmptyUsername_Test() {
        when(userDetailsService.loadUserByUsername(""))
                .thenThrow(new UsernameNotFoundException("User not found"));

        boolean result = authService.login("", USER_PASSWORD);

        assertThat(result).isFalse();
        verify(userDetailsService, times(1)).loadUserByUsername("");
        verify(encoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WithNullUsername_Test() {
        when(userDetailsService.loadUserByUsername(null))
                .thenThrow(new UsernameNotFoundException("User not found"));

        boolean result = authService.login(null, USER_PASSWORD);

        assertThat(result).isFalse();
        verify(userDetailsService, times(1)).loadUserByUsername(null);
        verify(encoder, never()).matches(anyString(), anyString());
    }

    // ===== ТЕСТЫ ДЛЯ register =====

    @Test
    void register_Success_Test() {
        Register register = createDefaultRegister();
        User user = createDefaultUser();

        when(userDetailsService.loadUserByUsername(USER_EMAIL))
                .thenThrow(new UsernameNotFoundException("User not found"));
        when(userMapper.toEntity(register)).thenReturn(user);
        when(encoder.encode(USER_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(user)).thenReturn(user);

        boolean result = authService.register(register);

        assertThat(result).isTrue();
        verify(userDetailsService, times(1)).loadUserByUsername(USER_EMAIL);
        verify(userMapper, times(1)).toEntity(register);
        verify(encoder, times(1)).encode(USER_PASSWORD);
        verify(userRepository, times(1)).save(user);
        assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
    }

    @Test
    void register_UserAlreadyExists_Test() {
        Register register = createDefaultRegister();

        when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(userDetails);

        boolean result = authService.register(register);

        assertThat(result).isFalse();
        verify(userDetailsService, times(1)).loadUserByUsername(USER_EMAIL);
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

        when(userDetailsService.loadUserByUsername("admin@mail.com"))
                .thenThrow(new UsernameNotFoundException("User not found"));
        when(userMapper.toEntity(register)).thenReturn(user);
        when(encoder.encode("Admin123@")).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(user)).thenReturn(user);

        boolean result = authService.register(register);

        assertThat(result).isTrue();
        verify(userDetailsService, times(1)).loadUserByUsername("admin@mail.com");
        verify(userMapper, times(1)).toEntity(register);
        verify(encoder, times(1)).encode("Admin123@");
        verify(userRepository, times(1)).save(user);
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    // ===== ТЕСТЫ НА ИСКЛЮЧЕНИЯ =====

    @Test
    void register_WithEmptyEmail_ThrowsException_Test() {
        // Проверяем, что при создании Register с пустым email выбрасывается исключение
        assertThatThrownBy(() -> new Register(
                "",
                USER_PASSWORD,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_PHONE,
                USER_ROLE
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Логин пользователя не может быть пустым!");
    }

    @Test
    void register_WithNullEmail_ThrowsException_Test() {
        assertThatThrownBy(() -> new Register(
                null,
                USER_PASSWORD,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_PHONE,
                USER_ROLE
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Логин пользователя не может быть пустым!");
    }

    @Test
    void register_WhenUserMapperReturnsNull_ThrowsNPE_Test() {
        Register register = createDefaultRegister();

        when(userDetailsService.loadUserByUsername(USER_EMAIL))
                .thenThrow(new UsernameNotFoundException("User not found"));
        when(userMapper.toEntity(register)).thenReturn(null);

        // Проверяем, что выбрасывается NPE
        assertThatThrownBy(() -> authService.register(register))
                .isInstanceOf(NullPointerException.class);

        verify(userDetailsService, times(1)).loadUserByUsername(USER_EMAIL);
        verify(userMapper, times(1)).toEntity(register);
        verify(encoder, times(1)).encode(USER_PASSWORD);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WhenUserRepositoryThrowsException_Test() {
        Register register = createDefaultRegister();
        User user = createDefaultUser();

        when(userDetailsService.loadUserByUsername(USER_EMAIL))
                .thenThrow(new UsernameNotFoundException("User not found"));
        when(userMapper.toEntity(register)).thenReturn(user);
        when(encoder.encode(USER_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(user)).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> authService.register(register))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(userDetailsService, times(1)).loadUserByUsername(USER_EMAIL);
        verify(userMapper, times(1)).toEntity(register);
        verify(encoder, times(1)).encode(USER_PASSWORD);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void register_WithEmptyPassword_ThrowsException_Test() {
        assertThatThrownBy(() -> new Register(
                USER_EMAIL,
                "",
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_PHONE,
                USER_ROLE
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Пароль не может быть пустым!");
    }

    @Test
    void register_WithNullPassword_ThrowsException_Test() {
        assertThatThrownBy(() -> new Register(
                USER_EMAIL,
                null,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_PHONE,
                USER_ROLE
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Пароль не может быть пустым!");
    }

    @Test
    void register_WithEmptyFirstName_ThrowsException_Test() {
        assertThatThrownBy(() -> new Register(
                USER_EMAIL,
                USER_PASSWORD,
                "",
                USER_LAST_NAME,
                USER_PHONE,
                USER_ROLE
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Имя пользователя не может быть пустым!");
    }

    @Test
    void register_WithEmptyLastName_ThrowsException_Test() {
        assertThatThrownBy(() -> new Register(
                USER_EMAIL,
                USER_PASSWORD,
                USER_FIRST_NAME,
                "",
                USER_PHONE,
                USER_ROLE
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Фамилия пользователя не может быть пустым!");
    }

    @Test
    void register_WithEmptyPhone_ThrowsException_Test() {
        assertThatThrownBy(() -> new Register(
                USER_EMAIL,
                USER_PASSWORD,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                "",
                USER_ROLE
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Телефон пользователя не может быть пустым!");
    }

    @Test
    void register_WithNullRole_ThrowsException_Test() {
        assertThatThrownBy(() -> new Register(
                USER_EMAIL,
                USER_PASSWORD,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_PHONE,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Роль пользователя не может быть пустым значением!");
    }
}