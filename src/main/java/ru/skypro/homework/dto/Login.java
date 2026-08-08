package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на аутентификацию пользователя (вход в систему)")
public record Login(

        @Schema(description = "Логин пользователя (email)",
                example = "john.doe@example.com",
                required = true,
                maxLength = 32)
        @NotBlank(message = "Логин не может быть пустым")
        @Email(message = "Логин должен быть корректным email адресом")
        @Size(max = 32, message = "Логин не может превышать 32 символа")
        String username,

        @Schema(description = "Пароль пользователя",
                example = "SecurePass123!",
                required = true,
                minLength = 8,
                maxLength = 16)
        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 8, max = 16, message = "Пароль должен содержать от 8 до 16 символов")
        String password

) {
}