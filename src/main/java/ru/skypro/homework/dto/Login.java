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
                maxLength = 50)
        @NotBlank(message = "Логин не может быть пустым")
        @Email(message = "Логин должен быть корректным email адресом")
        @Size(max = 50, message = "Логин не может превышать 50 символов")
        String username,

        @Schema(description = "Пароль пользователя",
                example = "SecurePass123!",
                required = true,
                minLength = 6,
                maxLength = 32)
        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 6, max = 32, message = "Пароль должен содержать от 6 до 32 символов")
        String password

) {
}