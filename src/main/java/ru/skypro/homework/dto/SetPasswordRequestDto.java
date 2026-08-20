package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
                example = "oldPassword123",
                required = true,
                minLength = 8,
                maxLength = 16,
                pattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")
        @NotBlank(message = "Текущий пароль не может быть пустым")
        @Size(min = 8, max = 16, message = "Пароль должен содержать от 8 до 16 символов")
        String currentPassword,

        /**
         * Новый пароль пользователя.
         * Обязательное поле: от 8 до 16 символов, должно содержать буквы и цифры.
         * Пример: newPassword456
         */
        @Schema(description = "Новый пароль пользователя",
                example = "newPassword456",
                required = true,
                minLength = 8,
                maxLength = 16,
                pattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")
        @NotBlank(message = "Новый пароль не может быть пустым")
        @Size(min = 8, max = 16, message = "Пароль должен содержать от 8 до 16 символов")
        String newPassword
) {
}
