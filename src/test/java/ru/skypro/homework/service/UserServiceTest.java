package ru.skypro.homework.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.constants.ExceptionMessages;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.dto.UserInfoResponseDto;
import ru.skypro.homework.dto.UserUpdateInfoDto;
import ru.skypro.homework.exception.InvalidPasswordException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.storage.FileStorageService;
import ru.skypro.homework.service.storage.StoredFileInfo;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // ===== КОНСТАНТЫ =====
    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "user@mail.com";
    private static final String USER_PASSWORD = "encodedPassword123@";
    private static final String USER_RAW_PASSWORD = "Password123@";
    private static final String USER_FIRST_NAME = "John";
    private static final String USER_LAST_NAME = "Doe";
    private static final String USER_PHONE = "+79991234567";
    private static final String USER_NEW_PHONE = "+79998887766";
    private static final String USER_FIRST_NAME_UPDATED = "Johnny";
    private static final String USER_LAST_NAME_UPDATED = "Doex";
    private static final Role USER_ROLE = Role.USER;
    private static final String USER_AVATAR_FILE_ID = "avatar123";
    private static final String USER_IMAGE_URL = "/images/avatars/user.jpg";

    private static final String NON_EXISTENT_EMAIL = "notfound@mail.com";
    private static final String OLD_PASSWORD = "oldPass123@";
    private static final String NEW_PASSWORD = "newPass456@";
    private static final String WRONG_PASSWORD = "wrongPassword";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private UserService userService;

    // ===== МЕТОДЫ-ФАБРИКИ ДЛЯ СОЗДАНИЯ ТЕСТОВЫХ ДАННЫХ =====

    private User createDefaultUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        user.setPassword(USER_PASSWORD);
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        user.setPhone(USER_PHONE);
        user.setRole(USER_ROLE);
        user.setAvatarFileId(USER_AVATAR_FILE_ID);
        return user;
    }

    private User createDefaultUserWithUpdatedFields() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        user.setPassword(USER_PASSWORD);
        user.setFirstName(USER_FIRST_NAME_UPDATED);
        user.setLastName(USER_LAST_NAME_UPDATED);
        user.setPhone(USER_NEW_PHONE);
        user.setRole(USER_ROLE);
        user.setAvatarFileId(USER_AVATAR_FILE_ID);
        return user;
    }

    private UserInfoResponseDto createDefaultUserInfoResponseDto() {
        return new UserInfoResponseDto(
                USER_ID,
                USER_EMAIL,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_PHONE,
                USER_ROLE,
                USER_IMAGE_URL
        );
    }

    private UserUpdateInfoDto createDefaultUserUpdateDto() {
        return new UserUpdateInfoDto(
                USER_FIRST_NAME_UPDATED,
                USER_LAST_NAME_UPDATED,
                USER_NEW_PHONE
        );
    }

    private UserUpdateInfoDto createDefaultUserUpdateDtoWithNullFields() {
        return new UserUpdateInfoDto(
                USER_FIRST_NAME_UPDATED,
                null,
                null
        );
    }

    private StoredFileInfo createDefaultStoredFileInfo() {
        return new StoredFileInfo("avatar123", "avatar.jpg", "image/jpeg", 1024L);
    }

    // ===== ТЕСТЫ ДЛЯ getUserInfo =====

    @Test
    void getUserInfo_Success_Test() {
        User user = createDefaultUser();
        UserInfoResponseDto expected = createDefaultUserInfoResponseDto();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        UserInfoResponseDto result = userService.getUserInfo(USER_EMAIL);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.email()).isEqualTo(USER_EMAIL);
        assertThat(result.firstName()).isEqualTo(USER_FIRST_NAME);
        assertThat(result.lastName()).isEqualTo(USER_LAST_NAME);
        assertThat(result.phone()).isEqualTo(USER_PHONE);
        assertThat(result.role()).isEqualTo(USER_ROLE);
        assertThat(result.image()).isEqualTo(USER_IMAGE_URL);

        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void getUserInfo_UserNotFound_Test() {
        when(userRepository.findByEmail(NON_EXISTENT_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(NON_EXISTENT_EMAIL))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(ExceptionMessages.formatUserNotFound(NON_EXISTENT_EMAIL));
    }

    // ===== ТЕСТЫ ДЛЯ passwordUpdate =====

    @Test
    void passwordUpdate_Success_Test() {
        User user = createDefaultUser();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(OLD_PASSWORD, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("encodedNewPassword");
        when(userRepository.save(user)).thenReturn(user);

        userService.passwordUpdate(USER_EMAIL, OLD_PASSWORD, NEW_PASSWORD);

        // Проверяем только факт вызова методов, не проверяем конкретные аргументы
        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(user);

        // Проверяем, что пароль обновился
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    void passwordUpdate_UserNotFound_Test() {
        when(userRepository.findByEmail(NON_EXISTENT_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.passwordUpdate(NON_EXISTENT_EMAIL, OLD_PASSWORD, NEW_PASSWORD))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(ExceptionMessages.formatUserNotFound(NON_EXISTENT_EMAIL));
    }

    @Test
    void passwordUpdate_InvalidCurrentPassword_Test() {
        User user = createDefaultUser();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(WRONG_PASSWORD, user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.passwordUpdate(USER_EMAIL, WRONG_PASSWORD, NEW_PASSWORD))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage(ExceptionMessages.invalidCurrentPassword());
    }

    // ===== ТЕСТЫ ДЛЯ updateUser =====

    @Test
    void updateUser_Success_Test() {
        User existingUser = createDefaultUser();
        User updatedUser = createDefaultUserWithUpdatedFields();
        UserUpdateInfoDto dto = createDefaultUserUpdateDto();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);

        UserUpdateInfoDto result = userService.updateUser(USER_EMAIL, dto);

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo(USER_FIRST_NAME_UPDATED);
        assertThat(result.lastName()).isEqualTo(USER_LAST_NAME_UPDATED);
        assertThat(result.phone()).isEqualTo(USER_NEW_PHONE);

        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_PartialUpdate_Test() {
        User existingUser = createDefaultUser();
        UserUpdateInfoDto dto = createDefaultUserUpdateDtoWithNullFields();

        User expectedUser = createDefaultUser();
        expectedUser.setFirstName(USER_FIRST_NAME_UPDATED);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(expectedUser);

        UserUpdateInfoDto result = userService.updateUser(USER_EMAIL, dto);

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo(USER_FIRST_NAME_UPDATED);
        assertThat(result.lastName()).isEqualTo(USER_LAST_NAME);
        assertThat(result.phone()).isEqualTo(USER_PHONE);

        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_UserNotFound_Test() {
        UserUpdateInfoDto dto = createDefaultUserUpdateDto();

        when(userRepository.findByEmail(NON_EXISTENT_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(NON_EXISTENT_EMAIL, dto))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(ExceptionMessages.formatUserNotFound(NON_EXISTENT_EMAIL));
    }

    // ===== ТЕСТЫ ДЛЯ findUserByPhone =====

    @Test
    void findUserByPhone_Success_Test() {
        User user = createDefaultUser();
        UserInfoResponseDto expected = createDefaultUserInfoResponseDto();

        when(userRepository.findByPhone(USER_PHONE)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        UserInfoResponseDto result = userService.findUserByPhone(USER_PHONE);

        assertThat(result).isNotNull();
        assertThat(result.phone()).isEqualTo(USER_PHONE);
        assertThat(result.email()).isEqualTo(USER_EMAIL);

        verify(userRepository, times(1)).findByPhone(USER_PHONE);
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void findUserByPhone_UserNotFound_Test() {
        String phone = "+79990000000";
        when(userRepository.findByPhone(phone)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByPhone(phone))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(ExceptionMessages.formatUserNotFound(""));
    }

    // ===== ТЕСТЫ ДЛЯ updateUsersAvatar =====

    @Test
    void updateUsersAvatar_Success_Test() throws IOException {
        User user = createDefaultUser();
        user.setAvatarFileId(null);

        MultipartFile file = mock(MultipartFile.class);
        StoredFileInfo storedFileInfo = createDefaultStoredFileInfo();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(file.getOriginalFilename()).thenReturn("avatar.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(fileStorageService.upload(any())).thenReturn(storedFileInfo);
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUsersAvatar(USER_EMAIL, file);

        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(fileStorageService, times(1)).upload(any());
        verify(userRepository, times(1)).save(user);
        assertThat(user.getAvatarFileId()).isEqualTo("avatar123");
    }

    @Test
    void updateUsersAvatar_UserNotFound_Test() throws IOException {
        MultipartFile file = mock(MultipartFile.class);

        when(userRepository.findByEmail(NON_EXISTENT_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUsersAvatar(NON_EXISTENT_EMAIL, file))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(ExceptionMessages.formatUserNotFound(NON_EXISTENT_EMAIL));
    }

    @Test
    void updateUsersAvatar_ReplaceExistingAvatar_Test() throws IOException {
        User user = createDefaultUser();
        user.setAvatarFileId("existingAvatarId");

        MultipartFile file = mock(MultipartFile.class);
        StoredFileInfo storedFileInfo = createDefaultStoredFileInfo();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(file.getOriginalFilename()).thenReturn("new_avatar.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(2048L);
        when(fileStorageService.replace(eq("existingAvatarId"), any())).thenReturn(storedFileInfo);
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUsersAvatar(USER_EMAIL, file);

        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(fileStorageService, times(1)).replace(eq("existingAvatarId"), any());
        verify(userRepository, times(1)).save(user);
        assertThat(user.getAvatarFileId()).isEqualTo("avatar123");
    }

    @Test
    void updateUsersAvatar_WithNullAvatarFileId_Test() throws IOException {
        User user = createDefaultUser();
        user.setAvatarFileId(null);

        MultipartFile file = mock(MultipartFile.class);
        StoredFileInfo storedFileInfo = createDefaultStoredFileInfo();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(file.getOriginalFilename()).thenReturn("avatar.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(fileStorageService.upload(any())).thenReturn(storedFileInfo);
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUsersAvatar(USER_EMAIL, file);

        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(fileStorageService, times(1)).upload(any());
        verify(userRepository, times(1)).save(user);
        assertThat(user.getAvatarFileId()).isEqualTo("avatar123");
    }

    @Test
    void updateUsersAvatar_WhenFileStorageReturnsNull_Test() throws IOException {
        User user = createDefaultUser();
        user.setAvatarFileId(null);

        MultipartFile file = mock(MultipartFile.class);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(file.getOriginalFilename()).thenReturn("avatar.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(fileStorageService.upload(any())).thenReturn(null);

        assertThatThrownBy(() -> userService.updateUsersAvatar(USER_EMAIL, file))
                .isInstanceOf(NullPointerException.class);

        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(fileStorageService, times(1)).upload(any());
        verify(userRepository, never()).save(user);
    }
}