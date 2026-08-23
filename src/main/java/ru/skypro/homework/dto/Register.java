package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ru.skypro.homework.constants.ValidationConstants;

/**
 * DTO для запроса на регистрацию нового пользователя.
 * <p>
 * Используется в POST‑запросе к эндпоинту /register. Содержит все необходимые данные
 * для создания учётной записи: логин (email), пароль, имя, фамилию, телефон и роль.
 * Валидация выполняется с помощью аннотаций Jakarta Validation; также предусмотрен
 * компактный конструктор для дополнительной проверки на null/пустые значения.
 * </p>
 */
@Schema(description = "Запрос на регистрацию нового пользователя")
public record Register(

        /**
         * Логин пользователя (используется как email).
         * Обязательное поле, должно быть непустым, соответствовать формату email,
         * не превышать 32 символа и удовлетворять заданному регулярному выражению.
         * Пример: john.doe@example.com
         */
        @Schema(description = "Логин пользователя (используется как email)",
                example = "john.doe@example.com",
                required = true,
                minLength = 4,
                maxLength = 32,
                pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        @NotBlank(message = "Логин пользователя не может быть пустым!")
        @Email(message = "Логин должен быть корректным email‑адресом")
        @Size(max = 32, message = "Логин не может превышать 32 символа")
        String username,

        /**
         * Пароль пользователя.
         * Обязательное поле: от 8 до 16 символов, должно содержать цифру, заглавную
         * и строчную букву, специальный символ из набора @#$%^&+=, не должно содержать пробелов.
         * Пример: SecurePass123!
         */
        @Schema(description = "Пароль пользователя. Должен содержать минимум 8 символов, " +
                "одну цифру, одну заглавную и одну строчную букву",
                example = "SecurePass123!",
                required = true,
                minLength = ValidationConstants.PASSWORD_MIN,
                maxLength = ValidationConstants.PASSWORD_MAX,
                pattern = ValidationConstants.PASSWORD_REGEX)
        @NotBlank(message = "Пароль не может быть пустым!")
        @Size(min = ValidationConstants.PASSWORD_MIN,
                max = ValidationConstants.PASSWORD_MAX,
                message = "Пароль должен содержать от " +
                        ValidationConstants.PASSWORD_MIN + " до " +
                        ValidationConstants.PASSWORD_MAX + " символов")
        @Pattern(
                regexp = ValidationConstants.PASSWORD_REGEX,
                message = ValidationConstants.PASSWORD_MESSAGE
        )
        String password,

        /**
         * Имя пользователя.
         * Обязательное поле: от 2 до 16 символов, может содержать только буквы, пробелы и дефисы.
         * Пример: John
         */
        @Schema(description = "Имя пользователя",
                example = "John",
                required = true,
                minLength = ValidationConstants.USERNAME_MIN,
                maxLength = ValidationConstants.USERNAME_MAX,
                pattern = ValidationConstants.USERNAME_REGEX)
        @NotBlank(message = "Имя пользователя не может быть пустым!")
        @Size(min = ValidationConstants.USERNAME_MIN,
                max = ValidationConstants.USERNAME_MAX,
                message = "Имя должно содержать от " +
                        ValidationConstants.USERNAME_MIN + " до " +
                        ValidationConstants.USERNAME_MAX + " символов")
        @Pattern(regexp = ValidationConstants.USERNAME_REGEX,
                message = ValidationConstants.USERNAME_MESSAGE)
        String firstName,

        /**
         * Фамилия пользователя.
         * Обязательное поле: от 2 до 16 символов, может содержать только буквы, пробелы и дефисы.
         * Пример: Doe
         */
        @Schema(description = "Фамилия пользователя",
                example = "Doe",
                required = true,
                minLength = ValidationConstants.USERNAME_MIN,
                maxLength = ValidationConstants.USERNAME_MAX,
                pattern = ValidationConstants.USERNAME_REGEX)
        @NotBlank(message = "Фамилия пользователя не может быть пустой!")
        @Size(min = ValidationConstants.USERNAME_MIN,
                max = ValidationConstants.USERNAME_MAX,
                message = "Фамилия должна содержать от " +
                        ValidationConstants.USERNAME_MIN + " до " +
                        ValidationConstants.USERNAME_MAX + " символов")
        @Pattern(regexp = ValidationConstants.USERNAME_REGEX,
                message = ValidationConstants.USERNAME_MESSAGE)
        String lastName,

        /**
         * Номер телефона пользователя.
         * Обязательное поле: от 10 до 20 символов, может начинаться с «+», допускаются цифры,
         * пробелы, дефисы и скобки.
         * Пример: +7 (999) 123-45-67
         */
        @Schema(description = "Номер телефона пользователя",
                example = "+7 (999) 123-45-67",
                required = true,
                minLength = ValidationConstants.PHONE_MIN,
                maxLength = ValidationConstants.PHONE_MAX,
                pattern = ValidationConstants.PHONE_REGEX)
        @NotBlank(message = "Телефон пользователя не может быть пустым!")
        @Pattern(regexp = ValidationConstants.PHONE_REGEX,
                message = ValidationConstants.PHONE_MESSAGE)
        String phone,

        /**
         * Роль пользователя в системе.
         * Обязательное поле; допустимые значения: USER, ADMIN.
         * Пример: USER
         */
        @Schema(description = "Роль пользователя в системе",
                example = "USER",
                required = true,
                allowableValues = {"USER", "ADMIN"})
        @NotNull(message = "Роль пользователя не может быть пустым значением!")
        Role role

) {
}