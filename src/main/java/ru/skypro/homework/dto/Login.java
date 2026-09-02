package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.skypro.homework.constants.ValidationConstants;

/**
 * DTO для запроса на аутентификацию пользователя (вход в систему).
 * <p>
 * Используется в POST‑запросе к эндпоинту /login. Содержит логин (email) и пароль
 * с набором валидационных ограничений: обязательность заполнения, проверка формата email,
 * ограничения по длине. Валидация выполняется на уровне фреймворка с помощью аннотаций Jakarta Validation.
 * </p>
 */
@Schema(description = "Запрос на аутентификацию пользователя (вход в систему)")
public record Login(

        /**
         * Логин пользователя (в формате email).
         * Обязательное поле, должно быть непустым, соответствовать формату email и не превышать 32 символа.
         * Пример: john.doe@example.com
         */
        @Schema(description = "Логин пользователя (email)",
                example = "john.doe@example.com",
                required = true,
                maxLength = 32)
        @NotBlank(message = "Логин не может быть пустым")
        @Email(message = "Логин должен быть корректным email‑адресом")
        @Size(max = 32, message = "Логин не может превышать 32 символа")
        String username,

        /**
         * Пароль пользователя.
         * Обязательное поле, должно быть непустым и содержать от 8 до 16 символов.
         * Пример: SecurePass123!
         */
        @Schema(description = "Пароль пользователя",
                example = "SecurePass123!",
                required = true,
                minLength = 8,
                maxLength = 16)
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
        String password
) {
}
