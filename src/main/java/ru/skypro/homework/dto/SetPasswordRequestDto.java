package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на смену пароля пользователя")
public record SetPasswordRequestDto(

        @Schema(description = "Текущий пароль пользователя",
                example = "oldPassword123",
                required = true,
                minLength = 8,
                maxLength = 16,
                pattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")
        @NotBlank(message = "Текущий пароль не может быть пустым")
        @Size(min = 8, max = 16, message = "Пароль должен содержать от 8 до 16 символов")
        String currentPassword,

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