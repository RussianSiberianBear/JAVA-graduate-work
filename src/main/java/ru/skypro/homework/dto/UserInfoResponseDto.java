package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о пользователе (ответ API)")
public record UserInfoResponseDto(

        @Schema(description = "Уникальный идентификатор пользователя",
                example = "12345",
                required = true)
        Long id,

        @Schema(description = "Адрес электронной почты пользователя",
                example = "john.doe@example.com",
                required = true,
                pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        String email,

        @Schema(description = "Имя пользователя",
                example = "John",
                required = true,
                minLength = 2,
                maxLength = 30)
        String firstName,

        @Schema(description = "Фамилия пользователя",
                example = "Doe",
                required = true,
                minLength = 2,
                maxLength = 30)
        String lastName,

        @Schema(description = "Номер телефона пользователя",
                example = "+7 (999) 123-45-67",
                required = true,
                pattern = "^\\+?[0-9\\s\\-()]{10,20}$")
        String phone,

        @Schema(description = "Роль пользователя в системе",
                example = "USER",
                required = true,
                allowableValues = {"USER", "ADMIN"})
        Role role,

        @Schema(description = "URL аватара пользователя",
                example = "/images/avatars/john_doe.jpg",
                type = "string",
                format = "uri")
        String avatarFileName

) {
}