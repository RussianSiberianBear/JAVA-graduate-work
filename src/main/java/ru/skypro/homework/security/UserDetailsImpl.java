package ru.skypro.homework.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.skypro.homework.model.User;

import java.util.Collection;
import java.util.List;

/**
 * Реализация интерфейса {@link UserDetails} для интеграции сущности {@link User}
 * с механизмом аутентификации Spring Security.
 * <p>
 * Оборачивает сущность User и предоставляет данные, необходимые для работы Spring Security:
 * пароль, имя пользователя (email), роли, флаги состояния учётной записи.
 * </p>
 */
public class UserDetailsImpl implements UserDetails {

    private final User user;

    /**
     * Конструктор, инициализирующий обёртку над сущностью User.
     *
     * @param user сущность пользователя, чьи данные будут использоваться для аутентификации
     */
    public UserDetailsImpl(User user) {
        this.user = user;
    }

    /**
     * Возвращает список полномочий (ролей) пользователя.
     * <p>
     * Преобразует роль из перечисления {@link ru.skypro.homework.dto.Role} в формат
     * Spring Security: префикс "ROLE_" + имя роли (например, ROLE_USER, ROLE_ADMIN).
     * В текущей реализации возвращается список из одного элемента. При необходимости
     * можно расширить логику для поддержки множественных ролей.
     * </p>
     *
     * @return коллекция GrantedAuthority, содержащая роли пользователя
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of((GrantedAuthority) () -> "ROLE_" + user.getRole().name());
    }

    /**
     * Возвращает пароль пользователя.
     * <p>
     * В реальном приложении здесь должен храниться хешированный пароль.
     * Метод вызывается Spring Security во время проверки учётных данных.
     * </p>
     *
     * @return пароль пользователя в виде строки
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Возвращает имя пользователя для целей аутентификации.
     * <p>
     * В данном проекте в качестве имени пользователя используется email.
     * </p>
     *
     * @return email пользователя
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Проверяет, не истёк ли срок действия учётной записи.
     * <p>
     * В рамках текущей бизнес‑логики учётные записи не имеют срока действия.
     * </p>
     *
     * @return true — учётная запись не истекла
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Проверяет, не заблокирована ли учётная запись.
     * <p>
     * В рамках текущей бизнес‑логики учётные записи не блокируются автоматически.
     * </p>
     *
     * @return true — учётная запись не заблокирована
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Проверяет, не истёк ли срок действия учётных данных (пароля).
     * <p>
     * В рамках текущей бизнес‑логики срок действия пароля не контролируется этим механизмом.
     * </p>
     *
     * @return true — учётные данные не истекли
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Проверяет, включена ли учётная запись.
     * <p>
     * В рамках текущей бизнес‑логики все созданные учётные записи считаются активными.
     * При необходимости можно добавить поле isActive в сущность User и использовать его здесь.
     * </p>
     *
     * @return true — учётная запись активна
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Возвращает исходную сущность User, инкапсулированную в данном объекте.
     * <p>
     * Используется, например, в {@link SecurityHelper} для доступа к дополнительным
     * данным пользователя (ID, ФИО и т. п.) без повторного запроса к БД.
     * </p>
     *
     * @return сущность User
     */
    public User getUser() {
        return user;
    }

    /**
     * Возвращает ID пользователя.
     * <p>
     * Удобный вспомогательный метод для быстрого доступа к идентификатору.
     * </p>
     *
     * @return ID пользователя
     */
    public Long getId() {
        return user.getId();
    }
}
