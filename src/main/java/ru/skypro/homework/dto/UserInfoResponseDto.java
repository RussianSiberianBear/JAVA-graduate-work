package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для представления информации о пользователе в ответе API.
 * <p>
 * Используется в GET‑запросе к эндпоинту /users/me для возврата данных
 * авторизованного пользователя. Содержит основные персональные данные:
 * идентификатор, email, имя, фамилию, телефон, роль и ссылку на аватар.
 * </p>
 */
@Schema(description = "Информация о пользователе (ответ API)")
public record UserInfoResponseDto(

        /**
         * Уникальный идентификатор пользователя в системе.
         * Пример: 12345
         */
        @Schema(description = "Уникальный идентификатор пользователя",
                example = "12345",
                required = true)
        Long id,

        /**
         * Адрес электронной почты пользователя.
         * Должен соответствовать стандартному формату email.
         * Пример: john.doe@example.com
         */
        @Schema(description = "Адрес электронной почты пользователя",
                example = "john.doe@example.com",
                required = true,
                pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        String email,

        /**
         * Имя пользователя.
         * Длина: от 2 до 30 символов.
         * Пример: John
         */
        @Schema(description = "Имя пользователя",
                example = "John",
                required = true,
                minLength = 2,
                maxLength = 30)
        String firstName,

        /**
         * Фамилия пользователя.
         * Длина: от 2 до 30 символов.
         * Пример: Doe
         */
        @Schema(description = "Фамилия пользователя",
                example = "Doe",
                required = true,
                minLength = 2,
                maxLength = 30)
        String lastName,

        /**
         * Номер телефона пользователя.
         * Должен содержать от 10 до 20 символов, может начинаться с «+»,
         * допускаются цифры, пробелы, дефисы и скобки.
         * Пример: +7 (999) 123-45-67
         */
        @Schema(description = "Номер телефона пользователя",
                example = "+7 (999) 123-45-67",
                required = true,
                pattern = "^\\+?[0-9\\s\\-()]{10,20}$")
        String phone,

        /**
         * Роль пользователя в системе.
         * Допустимые значения: USER, ADMIN.
         * Пример: USER
         */
        @Schema(description = "Роль пользователя в системе",
                example = "USER",
                required = true,
                allowableValues = {"USER", "ADMIN"})
        Role role,

        /**
         * URL‑адрес аватара пользователя.
         * Формат: URI.
         * Пример: /images/avatars/john_doe.jpg
         */
        @Schema(description = "URL аватара пользователя",
                example = "/images/avatars/john_doe.jpg",
                type = "string",
                format = "uri")
        String image
) {
}
