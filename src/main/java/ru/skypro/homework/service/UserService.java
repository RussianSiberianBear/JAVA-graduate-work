package ru.skypro.homework.service;

import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.SetPasswordResponseDto;
import ru.skypro.homework.dto.UserInfoResponseDto;
import ru.skypro.homework.exception.InvalidPasswordException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;

/**
 * Основной сервис по пользователю
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    /**
     * Метод выдает информацию по пользователю с именем(логином) username
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
     * @param username -  имя(логин) пользователя, для которого нужно обновить пароль
     * @param currentPassword - текущий пароль пользователя
     * @param newPassword - новый пароль пользователя
     * @return - DTO ответа по обрновлению паролля пользователя
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
}
