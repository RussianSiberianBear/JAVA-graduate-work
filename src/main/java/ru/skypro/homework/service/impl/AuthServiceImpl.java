package ru.skypro.homework.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
public class AuthServiceImpl implements AuthService {

    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param userDetailsService сервис для загрузки данных пользователя (для проверки при аутентификации)
     * @param userRepository      репозиторий для сохранения пользователей
     * @param userMapper          маппер для преобразования DTO в сущность и обратно
     * @param passwordEncoder     компонент для хеширования и проверки паролей
     */
    public AuthServiceImpl(UserDetailsService userDetailsService, UserRepository userRepository, UserMapper userMapper,
                           PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.encoder = passwordEncoder;
    }

    /**
     * Выполняет аутентификацию пользователя по логину (email) и паролю.
     * <p>
     * Пытается загрузить данные пользователя через {@link UserDetailsService}.
     * Если пользователь найден, проверяет пароль с помощью {@link PasswordEncoder}.
     * При отсутствии пользователя сразу возвращает false (без раскрытия деталей ошибки).
     * </p>
     *
     * @param userName  логин пользователя (в проекте — email)
     * @param password  пароль пользователя в открытом виде
     * @return true, если аутентификация успешна; false — в противном случае
     */
    @Override
    public boolean login(String userName, String password) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
            return encoder.matches(password, userDetails.getPassword());
        } catch (UsernameNotFoundException e) {
            // Пользователь не найден — считаем аутентификацию неуспешной
            return false;
        }
    }

    /**
     * Регистрирует нового пользователя.
     * <p>
     * Сначала проверяет, существует ли пользователь с таким email (username).
     * Если существует — регистрация отклоняется.
     * Если не существует — создаёт сущность, хеширует пароль, сохраняет в БД.
     * </p>
     *
     * @param register DTO с данными для регистрации (email, пароль, ФИО, телефон и т. п.)
     * @return true при успешной регистрации; false, если пользователь уже существует или произошла ошибка
     */
    @Override
    public boolean register(Register register) {
        try {
            // Проверяем, существует ли уже пользователь с таким username (email)
            userDetailsService.loadUserByUsername(register.username());
            // Если исключение не выброшено — пользователь уже есть, регистрация невозможна
            return false;
        } catch (UsernameNotFoundException e) {
            // Исключение означает, что пользователя нет — можно регистрировать
            User user = userMapper.toEntity(register);
            user.setPassword(encoder.encode(register.password()));
            userRepository.save(user);
            return true;
        }
    }
}
