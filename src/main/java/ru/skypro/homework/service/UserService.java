package ru.skypro.homework.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.SetPasswordResponseDto;
import ru.skypro.homework.dto.UserInfoResponseDto;
import ru.skypro.homework.exception.InvalidPasswordException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;

import java.util.Optional;

@PreAuthorize("hasAnyRole('ADMIN','USER')")
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

    public UserInfoResponseDto getUserInfo(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь " + username + " не найден!"));
    }

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
