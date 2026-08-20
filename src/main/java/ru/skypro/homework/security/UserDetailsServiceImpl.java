package ru.skypro.homework.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.skypro.homework.repository.UserRepository;

/**
 * Реализация сервиса загрузки данных пользователя для Spring Security.
 * <p>
 * Отвечает за поиск пользователя по имени пользователя (в данном проекте — по email)
 * и оборачивание найденной сущности в объект {@link UserDetailsImpl}, совместимый
 * с механизмом аутентификации Spring Security. Если пользователь не найден,
 * выбрасывается {@link UsernameNotFoundException}.
 * </p>
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Конструктор, внедряющий репозиторий для доступа к данным пользователей.
     *
     * @param userRepository репозиторий {@link UserRepository} для поиска пользователей
     */
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Загружает данные пользователя по имени пользователя.
     * <p>
     * В рамках данной реализации в качестве имени пользователя используется email.
     * Метод ищет пользователя в БД, оборачивает его в {@link UserDetailsImpl}
     * и возвращает для дальнейшей проверки пароля и аутентификации.
     * </p>
     *
     * @param email email пользователя (используется как username)
     * @return объект {@link UserDetails}, содержащий данные пользователя и его роли
     * @throws UsernameNotFoundException если пользователь с указанным email не найден
     */
    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
    }
}
