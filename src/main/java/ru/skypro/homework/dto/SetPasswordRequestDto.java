package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.skypro.homework.constants.ValidationConstants;

/**
 * DTO для запроса на смену пароля пользователя.
 * <p>
 * Используется в POST‑запросе к эндпоинту /users/set_password. Содержит текущий
 * и новый пароль с набором валидационных ограничений: обязательность заполнения,
 * ограничения по длине (от 8 до 16 символов) и требования к составу (наличие букв и цифр).
 * Валидация выполняется на уровне фреймворка с помощью аннотаций Jakarta Validation.
 * </p>
 */
@Schema(description = "Запрос на смену пароля пользователя")
public record SetPasswordRequestDto(

        /**
         * Текущий пароль пользователя.
         * Обязательное поле: от 8 до 16 символов, должно содержать буквы и цифры.
         * Пример: oldPassword123
         */
        @Schema(description = "Текущий пароль пользователя",
                example = "oldPassword123@",
                required = true,
                minLength = ValidationConstants.PASSWORD_MIN,
                maxLength = ValidationConstants.PASSWORD_MAX,
                pattern = ValidationConstants.PASSWORD_REGEX)
        @NotBlank(message = "Текущий пароль не может быть пустым")
        @Size(min = ValidationConstants.PASSWORD_MIN,
                max = ValidationConstants.PASSWORD_MAX,
                message = "Пароль должен содержать от " +
                        ValidationConstants.PASSWORD_MIN + " до " +
                        ValidationConstants.PASSWORD_MAX + " символов")
        @Pattern(
                regexp = ValidationConstants.PASSWORD_REGEX,
                message = ValidationConstants.PASSWORD_MESSAGE
        )
        String currentPassword,

        /**
         * Новый пароль пользователя.
         * Обязательное поле: от 8 до 16 символов, должно содержать буквы и цифры.
         * Пример: newPassword456
         */
        @Schema(description = "Новый пароль пользователя",
                example = "newPassword456@",
                required = true,
                minLength = ValidationConstants.PASSWORD_MIN,
                maxLength = ValidationConstants.PASSWORD_MAX,
                pattern = ValidationConstants.PASSWORD_REGEX)
        @NotBlank(message = "Новый пароль не может быть пустым")
        @Size(min = ValidationConstants.PASSWORD_MIN,
                max = ValidationConstants.PASSWORD_MAX,
                message = "Пароль должен содержать от " +
                        ValidationConstants.PASSWORD_MIN + " до " +
                        ValidationConstants.PASSWORD_MAX + " символов")
        @Pattern(
                regexp = ValidationConstants.PASSWORD_REGEX,
                message = ValidationConstants.PASSWORD_MESSAGE
        )
        String newPassword
) {
}