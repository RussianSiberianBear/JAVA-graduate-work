package ru.skypro.homework.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.skypro.homework.dto.Role;

/**
 * Сущность пользователя (User) для хранения учётных данных и профиля.
 * <p>
 * Соответствует таблице "user" в базе данных (имя экранировано кавычками из‑за совпадения
 * с зарезервированным словом в некоторых СУБД). Содержит основные данные пользователя:
 * email, пароль, ФИО, телефон, роль и ссылку на аватар.
 * </p>
 */
@Data
@Entity
@NoArgsConstructor  // Конструктор без параметров (требуется для JPA)
@AllArgsConstructor // Конструктор со всеми параметрами (удобен для тестов и фабрик)
@Table(name = "\"user\"")
public class User {

    /**
     * Уникальный идентификатор пользователя.
     * Генерируется автоматически базой данных (стратегия IDENTITY).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Электронная почта пользователя.
     * <p>
     * Обязательное поле, должно быть уникальным (unique = true), длина ограничена 32 символами.
     * Используется как основной идентификатор для входа (логин).
     * </p>
     */
    @Column(nullable = false, unique = true, length = 32)
    private String email;

    /**
     * Пароль пользователя.
     * <p>
     * Обязательное поле. В реальном приложении здесь хранится не открытый текст,
     * а хешированное значение (например, BCrypt).
     * </p>
     */
    @Column(nullable = false)
    private String password;

    /**
     * Имя пользователя.
     * Обязательное поле, длина ограничена 32 символами.
     */
    @Column(nullable = false, length = 32)
    private String firstName;

    /**
     * Фамилия пользователя.
     * Обязательное поле, длина ограничена 32 символами.
     */
    @Column(nullable = false, length = 32)
    private String lastName;

    /**
     * Телефон пользователя.
     * Обязательное поле, длина ограничена 32 символами.
     */
    @Column(nullable = false, length = 32)
    private String phone;

    /**
     * Роль пользователя в системе.
     * <p>
     * Хранится в виде строки (EnumType.STRING) для читаемости в БД.
     * Значение по умолчанию — Role.USER. Длина ограничена 32 символами.
     * Возможные значения определяются перечислением {@link Role}.
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Role role = Role.USER;

    /**
     * Идентификатор файла аватара пользователя в хранилище.
     * <p>
     * Необязательное поле (может быть null). Длина ограничена 255 символами.
     * На основе этого ID мапперы формируют URL вида "/images/{avatarFileId}"
     * для передачи клиенту.
     * </p>
     */
    @Column(length = 255)
    private String avatarFileId;
}
