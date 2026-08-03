package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на обновление данных пользователя (частичное обновление)")
public record UserUpdateInfoDto(

        @Schema(description = "Имя пользователя",
                example = "John",
                minLength = 2,
                maxLength = 32,
                pattern = "^[A-Za-zА-Яа-я\\s-]+$",
                nullable = true)
        @Size(min = 2, max = 32, message = "Имя должно содержать от 2 до 32 символов")
        @Pattern(regexp = "^[A-Za-zА-Яа-я\\s-]+$",
                message = "Имя может содержать только буквы, пробелы и дефисы")
        String firstName,

        @Schema(description = "Фамилия пользователя",
                example = "Doe",
                minLength = 2,
                maxLength = 32,
                pattern = "^[A-Za-zА-Яа-я\\s-]+$",
                nullable = true)
        @Size(min = 2, max = 32, message = "Фамилия должна содержать от 2 до 32 символов")
        @Pattern(regexp = "^[A-Za-zА-Яа-я\\s-]+$",
                message = "Фамилия может содержать только буквы, пробелы и дефисы")
        String lastName,

        @Schema(description = "Номер телефона пользователя",
                example = "+7 (999) 123-45-67",
                pattern = "^\\+?[0-9\\s\\-()]{10,20}$",
                nullable = true)
        @Pattern(regexp = "^\\+?[0-9\\s\\-()]{10,20}$",
                message = "Телефон должен содержать от 10 до 20 цифр, может начинаться с +")
        String phone

) {
}