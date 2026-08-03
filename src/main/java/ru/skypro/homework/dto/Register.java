package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Запрос на регистрацию нового пользователя")
public record Register(

        @Schema(description = "Логин пользователя (используется как email)",
                example = "john.doe@example.com",
                required = true,
                pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        @NotBlank(message = "Логин пользователя не может быть пустым!")
        @Email(message = "Логин должен быть корректным email адресом")
        @Size(max = 50, message = "Логин не может превышать 50 символов")
        String username,

        @Schema(description = "Пароль пользователя. Должен содержать минимум 8 символов, " +
                "одну цифру, одну заглавную и одну строчную букву",
                example = "SecurePass123!",
                required = true,
                minLength = 8,
                maxLength = 64,
                pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")
        @NotBlank(message = "Пароль не может быть пустым!")
        @Size(min = 8, max = 64, message = "Пароль должен содержать от 8 до 64 символов")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
                message = "Пароль должен содержать минимум 8 символов, " +
                        "одну цифру, одну заглавную и одну строчную букву, " +
                        "один специальный символ (@#$%^&+=), и не содержать пробелов"
        )
        String password,

        @Schema(description = "Имя пользователя",
                example = "John",
                required = true,
                minLength = 2,
                maxLength = 30,
                pattern = "^[A-Za-zА-Яа-я\\s-]+$")
        @NotBlank(message = "Имя пользователя не может быть пустым!")
        @Size(min = 2, max = 30, message = "Имя должно содержать от 2 до 30 символов")
        @Pattern(regexp = "^[A-Za-zА-Яа-я\\s-]+$",
                message = "Имя может содержать только буквы, пробелы и дефисы")
        String firstName,

        @Schema(description = "Фамилия пользователя",
                example = "Doe",
                required = true,
                minLength = 2,
                maxLength = 30,
                pattern = "^[A-Za-zА-Яа-я\\s-]+$")
        @NotBlank(message = "Фамилия пользователя не может быть пустой!")
        @Size(min = 2, max = 30, message = "Фамилия должна содержать от 2 до 30 символов")
        @Pattern(regexp = "^[A-Za-zА-Яа-я\\s-]+$",
                message = "Фамилия может содержать только буквы, пробелы и дефисы")
        String lastName,

        @Schema(description = "Номер телефона пользователя",
                example = "+7 (999) 123-45-67",
                required = true,
                pattern = "^\\+?[0-9\\s\\-()]{10,20}$")
        @NotBlank(message = "Телефон пользователя не может быть пустым!")
        @Pattern(regexp = "^\\+?[0-9\\s\\-()]{10,20}$",
                message = "Телефон должен содержать от 10 до 20 цифр, может начинаться с +")
        String phone,

        @Schema(description = "Роль пользователя в системе",
                example = "USER",
                required = true,
                allowableValues = {"USER", "ADMIN", "MODERATOR"})
        @NotNull(message = "Роль пользователя не может быть пустым значением!")
        Role role

) {
    // Компактный конструктор для валидации (оставляем как есть,
    // но теперь валидация дублируется аннотациями выше)
    public Register {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Логин пользователя не может быть пустым!");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Пароль не может быть пустым!");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым!");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Фамилия пользователя не может быть пустым!");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Телефон пользователя не может быть пустым!");
        }
        if (role == null) {
            throw new IllegalArgumentException("Роль пользователя не может быть пустым значением!");
        }
    }
}