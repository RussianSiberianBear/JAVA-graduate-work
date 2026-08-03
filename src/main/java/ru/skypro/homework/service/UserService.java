package ru.skypro.homework.service;

import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.SetPasswordResponseDto;
import ru.skypro.homework.dto.UserInfoResponseDto;
import ru.skypro.homework.dto.UserUpdateInfoDto;
import ru.skypro.homework.exception.InvalidPasswordException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;

import java.io.IOException;

/**
 * Основной сервис по пользователю
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final FileService fileService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, FileService fileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.fileService = fileService;
    }


    /**
     * Метод выдает информацию по пользователю с именем(логином) username
     *
     * @param username - имя(логин) пользователя
     * @return DTO с данными пользователя
     */
    public UserInfoResponseDto getUserInfo(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь " + username + " не найден!"));
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
    public SetPasswordResponseDto passwordUpdate(String username, String currentPassword, String newPassword) {

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь " + username + " не найден!"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException("Неверный текущий пароль");
        }

        String oldPassword = user.getPassword();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return new SetPasswordResponseDto(oldPassword, newPassword);
    }

    public UserInfoResponseDto findUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь  не найден!"));
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

        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> new UsernameNotFoundException("Пользователь " + username + " не найден!"));
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

    @Transactional
    public void updateUsersAvatar(String username, MultipartFile file) throws IOException {

        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> new UsernameNotFoundException("Пользователь " + username + " не найден!"));
        fileService.uploadAvatarFile(user,file);
    }

}
