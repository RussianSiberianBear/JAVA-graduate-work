package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на смену пароля пользователя")
public record SetPasswordResponseDto(

        @Schema(description = "Текущий пароль пользователя",
                example = "oldPassword123",
                required = true,
                minLength = 6,
                maxLength = 32,
                pattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")
        String currentPassword,

        @Schema(description = "Новый пароль пользователя",
                example = "newPassword456",
                required = true,
                minLength = 6,
                maxLength = 32,
                pattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")
        String newPassword

) {
}