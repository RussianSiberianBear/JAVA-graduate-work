package ru.skypro.homework.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
import ru.skypro.homework.service.storage.FileUploadRequest;
import ru.skypro.homework.service.storage.StoredFileInfo;

import java.io.IOException;

/**
 * Основной сервис по работе с пользователями.
 * <p>
 * Предоставляет методы для получения информации о пользователе, обновления пароля,
 * личных данных и аватара. Сервис обеспечивает проверку подлинности пароля,
 * управление файлами аватаров через координатор замены файлов и корректную обработку ошибок.
 * </p>
 */
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final FileReplacementCoordinator fileReplacementCoordinator;

    /**
     * Конструктор сервиса.
     *
     * @param userRepository             репозиторий для работы с пользователями
     * @param passwordEncoder            компонент для кодирования и проверки паролей
     * @param userMapper                 маппер для преобразования сущностей в DTO и обратно
     * @param fileReplacementCoordinator координатор замены файлов (управление старыми и новыми файлами)
     */
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper,
                       FileReplacementCoordinator fileReplacementCoordinator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.fileReplacementCoordinator = fileReplacementCoordinator;
    }

    /**
     * Возвращает информацию о пользователе по его email (логину).
     *
     * @param username email (логин) пользователя
     * @return {@link UserInfoResponseDto} с данными пользователя
     * @throws UsernameNotFoundException если пользователь с указанным email не найден
     */
    public UserInfoResponseDto getUserInfo(String username) {
        return userRepository.findByEmail(username)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionMessages.formatUserNotFound(username)));
    }

    /**
     * Обновляет пароль пользователя.
     * <p>
     * Метод проверяет текущий пароль с помощью {@link PasswordEncoder}, и если он корректен,
     * кодирует новый пароль и сохраняет изменения в БД.
     * </p>
     *
     * @param username        email (логин) пользователя, для которого нужно обновить пароль
     * @param currentPassword текущий пароль пользователя
     * @param newPassword     новый пароль пользователя
     * @throws UsernameNotFoundException если пользователь не найден
     * @throws InvalidPasswordException  если текущий пароль неверен
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

    /**
     * Находит пользователя по номеру телефона и возвращает его данные.
     *
     * @param phone номер телефона пользователя
     * @return {@link UserInfoResponseDto} с данными пользователя
     * @throws UsernameNotFoundException если пользователь с указанным телефоном не найден
     */
    public UserInfoResponseDto findUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionMessages.formatUserNotFound("")));
    }

    /**
     * Обновляет личные данные пользователя (имя, фамилию, телефон).
     * <p>
     * Можно обновлять любое подмножество полей — изменяются только те, что переданы в DTO.
     * </p>
     *
     * @param username email (логин) пользователя
     * @param dto      DTO с обновляемыми данными пользователя
     * @return {@link UserUpdateInfoDto} с актуальными именем, фамилией и телефоном пользователя
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Transactional
    public UserUpdateInfoDto updateUser(String username, UserUpdateInfoDto dto) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionMessages.formatUserNotFound(username)));

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
     * Обновляет аватар пользователя.
     * <p>
     * Использует координатор замены файлов для загрузки нового изображения и регистрации
     * его как замены старого. Старое изображение не удаляется сразу, а будет обработано
     * координатором в соответствии с его логикой.
     * </p>
     *
     * @param username email (логин) пользователя
     * @param file     загружаемый файл аватара
     * @throws IOException          если произошла ошибка ввода-вывода при работе с файлом
     * @throws FileStorageException если не удалось обработать файл в хранилище
     * @throws UsernameNotFoundException если пользователь не найден
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
