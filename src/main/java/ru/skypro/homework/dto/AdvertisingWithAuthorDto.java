package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO одного объявления автора")
public record AdvertisingWithAuthorDto(
        @Schema(description = "Идентификатор объявления", example = "1")
        Long pk,
        @Schema(description = "Имя автора", example = "Joe")
        String authorFirstName,

        @Schema(description = "Фамилия автора", example = "Doe")
        String authorLastName,

        @Schema(description = "Описание объявления", example = "В данном объявлении рекламируется корм для животных")
        String description,

        @Schema(description = "E-mail автора", example = "youmail@google.com")
        String email,

        @Schema(description = "Файл рисунка объявления", example = "pictute.jpg")
        String image,

        @Schema(description = "Телефон пользователя", example = "+7499 123123")
        String phone,

        @Schema(description = "Стоимость объявления", example = "123")
        String price,

        @Schema(description = "Заголовок объявления", example = "Корм для живтоных")
        String title
) {
}