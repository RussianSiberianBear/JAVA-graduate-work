package ru.skypro.homework.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ru.skypro.homework.dto.Register;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AuthService;

/**
 * Реализация сервиса аутентификации и регистрации пользователей.
 * <p>
 * Предоставляет методы для:
 * - проверки учётных данных при входе (login);
 * - регистрации нового пользователя с проверкой уникальности email и хешированием пароля.
 * </p>
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param userRepository репозиторий для сохранения пользователей
     * @param userMapper     маппер для преобразования DTO в сущность и обратно
     * @param passwordEncoder компонент для хеширования и проверки паролей
     */
    public AuthServiceImpl(UserRepository userRepository, UserMapper userMapper,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.encoder = passwordEncoder;
    }

    /**
     * Выполняет аутентификацию пользователя по логину (email) и паролю.
     * <p>
     * Находит пользователя по email через {@link UserRepository} и проверяет пароль
     * с помощью {@link PasswordEncoder}. При отсутствии пользователя возвращается false
     * (без раскрытия деталей ошибки для безопасности).
     * </p>
     *
     * @param userName логин пользователя (в проекте — email)
     * @param password пароль пользователя в открытом виде
     * @return true, если аутентификация успешна; false — в противном случае
     */
    @Override
    public boolean login(String userName, String password) {
        return userRepository.findByEmail(userName)
                .map(user -> encoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    /**
     * Регистрирует нового пользователя.
     * <p>
     * Сначала проверяет, существует ли пользователь с таким email (username).
     * Если существует—регистрация отклоняется.
     * Если не существует—создаёт сущность, хеширует пароль, сохраняет в БД.
     * </p>
     *
     * @param register DTO с данными для регистрации (email, пароль, ФИО, телефон и т.п.)
     * @return true при успешной регистрации; false, если пользователь уже существует или произошла ошибка
     */
    @Override
    @Transactional
    public boolean register(Register register) {
        // Явная проверка существования пользователя через репозиторий
        if (userRepository.existsByEmail(register.username())) {
            log.warn("Registration failed: user already exists with email: {}", register.username());
            return false;
        }

        User user = userMapper.toEntity(register);
        user.setPassword(encoder.encode(register.password()));
        userRepository.save(user);
        return true;
    }
}
