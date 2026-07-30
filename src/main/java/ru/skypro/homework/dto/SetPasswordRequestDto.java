package ru.skypro.homework.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetPasswordRequestDto(
        @NotBlank(message = "Текущий пароль не может быть пустым")
        String currentPassword,

        @NotBlank(message = "Новый пароль не может быть пустым")
        @Size(min = 6,
                message = "Новый пароль должен содержать минимум 6 символов")
        String newPassword
) {}