package ru.skypro.homework.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.config.StorageDirectories;
import ru.skypro.homework.constants.ExceptionMessages;
import ru.skypro.homework.dto.UserInfoResponseDto;
import ru.skypro.homework.dto.UserUpdateInfoDto;
import ru.skypro.homework.exception.FileStorageException;
import ru.skypro.homework.exception.InvalidPasswordException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.storage.FileReplacementCoordinator;
import ru.skypro.homework.service.storage.FileStorageService;
import ru.skypro.homework.service.storage.FileUploadRequest;
import ru.skypro.homework.service.storage.StoredFileInfo;

import java.io.IOException;

/**
 * Основной сервис по пользователю
 */
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final FileStorageService fileService;
    private final FileReplacementCoordinator fileReplacementCoordinator;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper,
                       FileStorageService fileService, FileReplacementCoordinator fileReplacementCoordinator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.fileService = fileService;
        this.fileReplacementCoordinator = fileReplacementCoordinator;
    }

    /**
     * Метод выдает информацию по пользователю с именем(логином) username
     *
     * @param username - имя(логин) пользователя
     * @return DTO с данными пользователя
     */
    public UserInfoResponseDto getUserInfo(String username) {

        return userRepository.findByEmail(username)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionMessages.formatUserNotFound(username)));
    }

    /**
     * Метод обновляет пароль пользователя
     *
     * @param username        -  имя(логин) пользователя, для которого нужно обновить пароль
     * @param currentPassword - текущий пароль пользователя
     * @param newPassword     - новый пароль пользователя
     * @return - DTO ответа по обрновлению пароля пользователя
     */
    @Transactional
    public void passwordUpdate(String username, String currentPassword, String newPassword) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionMessages.formatUserNotFound(username)));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException(ExceptionMessages.invalidCurrentPassword());
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserInfoResponseDto findUserByPhone(String phone) {

        return userRepository.findByPhone(phone)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionMessages.formatUserNotFound("")));
    }

    /**
     * Метод обновляет данные пользователя -имя, фамилию, телефон (все вместе или то что надо)
     * Выбрасывает исключение UsernameNotFoundException, если пользователь не найден
     *
     * @param username - имя(логин) пользователя
     * @param dto      - обновляемая информация пользователя
     * @return DTO в виде фамилии, имени и телефона пользователя
     */
    @Transactional
    public UserUpdateInfoDto updateUser(String username, UserUpdateInfoDto dto) {

        User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(ExceptionMessages.formatUserNotFound(username)));
        if (dto.firstName() != null) {
            user.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null) {
            user.setLastName(dto.lastName());
        }
        if (dto.phone() != null) {
            user.setPhone(dto.phone());
        }
        user = userRepository.save(user);

        return new UserUpdateInfoDto(user.getFirstName(), user.getLastName(), user.getPhone());
    }

    /**
     * Метод обновляет аватар пользователя
     *
     * @param username - логин пользователя
     * @param file     - файл аватарки пользователя
     * @throws IOException - проверяемое исключение ввода-вывода
     */
    @Transactional
    public void updateUsersAvatar(String username, MultipartFile file) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                ExceptionMessages.formatUserNotFound(username)
                        )
                );

        String oldFileId = user.getAvatarFileId();

        try {
            FileUploadRequest request = new FileUploadRequest(
                    StorageDirectories.AVATARS,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream()
            );

            StoredFileInfo newFile =
                    fileReplacementCoordinator.uploadAndRegisterReplacement(
                            request,
                            oldFileId
                    );

            user.setAvatarFileId(newFile.id());

            log.info(
                    "Avatar updated for user: {}, oldFileId: {}, newFileId: {}",
                    username,
                    oldFileId,
                    newFile.id()
            );

        } catch (IOException e) {
            log.error("Failed to read avatar file for user: {}", username, e);

            throw new FileStorageException(
                    "Failed to read uploaded avatar: " + e.getMessage(),
                    e
            );
        }
    }

}
