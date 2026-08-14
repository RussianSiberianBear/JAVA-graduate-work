package ru.skypro.homework.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.skypro.homework.model.User;

import java.util.Collection;


@Component
public class SecurityHelper {

    /**
     * Получить ID текущего авторизованного пользователя
     */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Получить текущего авторизованного пользователя
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl user) {
            return user.getUser();
        }

        return null;
    }

    /**
     * Получить имя текущего пользователя
     */
    public String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * Проверить, авторизован ли пользователь
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String);
    }

    public boolean isAdmin() {
        Collection<? extends GrantedAuthority> authorities = getAuthorities();
        return authorities != null && authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Получить все роли авторизованного пользователя
     *
     * @return Коллеция ролей
     */
    public Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getAuthorities() : null;
    }
}
