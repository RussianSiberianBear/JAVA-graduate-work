package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.skypro.homework.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link User}.
 * <p>
 * Расширяет {@link JpaRepository}, предоставляя базовые CRUD‑операции,
 * а также содержит дополнительные методы для поиска пользователей и выборки
 * идентификаторов аватаров:
 * - поиск по email и телефону;
 * - массовая выборка avatarFileId для операций с файловым хранилищем.
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по адресу электронной почты.
     * <p>
     * Поле email уникально, поэтому возвращается не более одного пользователя.
     * </p>
     *
     * @param email адрес электронной почты пользователя
     * @return Optional с найденным пользователем либо пустой Optional, если пользователь не найден
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверяет существование пользователя по адресу электронной почты.
     *
     * @param email адрес электронной почты для проверки
     * @return true, если пользователь с таким email существует; false — в противном случае
     */
    boolean existsByEmail(String email);

    /**
     * Находит пользователя по номеру телефона.
     * <p>
     * Телефон в модели помечен как обязательный, но не уникальный — в теории
     * может быть несколько пользователей с одинаковым номером (зависит от бизнес‑правил).
     * Однако метод возвращает Optional, предполагая, что в рамках текущей логики
     * ожидается не более одного совпадения.
     * </p>
     *
     * @param phone номер телефона пользователя
     * @return Optional с найденным пользователем либо пустой Optional
     */
    Optional<User> findByPhone(String phone);

    /**
     * Возвращает список идентификаторов файлов аватаров для всех пользователей,
     * у которых аватар задан (avatarFileId не null).
     * <p>
     * Используется, например, для массовой очистки хранилища изображений
     * при удалении пользователей или для аудита загруженных файлов.
     * </p>
     *
     * @return список avatarFileId (строк), не содержащих null
     */
    @Query("""
            select u.avatarFileId
            from User u
            where u.avatarFileId is not null
            """)
    List<String> findAllAvatarFileIds();
}
