package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Данные для входа в систему")
public record Login(
        @Schema(description = "Имя пользователя", example = "john_doe", required = true)
        String username,

        @Schema(description = "Пароль пользователя", example = "securePass123", required = true)
        String password
) {
}