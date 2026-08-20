package ru.skypro.homework.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.skypro.homework.model.User;

import java.util.Collection;

/**
 * Вспомогательный класс для работы с контекстом безопасности.
 * <p>
 * Предоставляет удобные методы для получения данных о текущем авторизованном пользователе:
 * ID, email, проверку авторизации, проверку роли администратора и список ролей.
 * </p>
 */
@Component
public class SecurityHelper {

    /**
     * Получить ID текущего авторизованного пользователя.
     *
     * @return ID пользователя, либо null, если пользователь не авторизован
     */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Получить текущего авторизованного пользователя.
     * <p>
     * Извлекает объект User из SecurityContext, проверяя, что аутентификация активна
     * и principal имеет ожидаемый тип (UserDetailsImpl).
     * </p>
     *
     * @return объект User, либо null, если пользователь не авторизован или principal
     *         имеет неподходящий тип
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // Предполагается, что principal — это экземпляр UserDetailsImpl,
        // который оборачивает сущность User
        if (principal instanceof UserDetailsImpl user) {
            return user.getUser();
        }

        return null;
    }

    /**
     * Получить имя текущего пользователя.
     * <p>
     * В рамках данной реализации в качестве имени пользователя используется email.
     * </p>
     *
     * @return email пользователя, либо null, если пользователь не авторизован
     */
    public String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * Проверить, авторизован ли пользователь.
     * <p>
     * Условие «не instanceof String» добавлено для защиты от некоторых сценариев,
     * когда в principal может оказаться строка (например, при анонимной аутентификации
     * с дефолтным principal).
     * </p>
     *
     * @return true, если пользователь авторизован и principal корректен; иначе false
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String);
    }

    /**
     * Проверить, имеет ли текущий пользователь роль администратора.
     * <p>
     * Проверяет наличие полномочия ROLE_ADMIN в списке authorities.
     * </p>
     *
     * @return true, если у пользователя есть роль ROLE_ADMIN; иначе false
     */
    public boolean isAdmin() {
        Collection<? extends GrantedAuthority> authorities = getAuthorities();
        return authorities != null && authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Получить все роли (полномочия) текущего авторизованного пользователя.
     *
     * @return коллекция GrantedAuthority, либо null, если аутентификация отсутствует
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getAuthorities() : null;
    }
}
