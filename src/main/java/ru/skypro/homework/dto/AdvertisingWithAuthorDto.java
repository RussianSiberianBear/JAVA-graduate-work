package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для представления одного объявления вместе с данными его автора.
 * <p>
 * Используется в ответах контроллера при получении детальной информации об объявлении,
 * когда требуется отобразить не только данные самого объявления, но и контактные
 * и персональные сведения автора (имя, фамилия, email, телефон и т. д.).
 * </p>
 */
@Schema(description = "DTO одного объявления автора")
public record AdvertisingWithAuthorDto(

        /**
         * Уникальный идентификатор объявления.
         * Пример: 1
         */
        @Schema(description = "Идентификатор объявления", example = "1")
        Long pk,

        /**
         * Имя автора объявления.
         * Пример: Joe
         */
        @Schema(description = "Имя автора", example = "Joe")
        String authorFirstName,

        /**
         * Фамилия автора объявления.
         * Пример: Doe
         */
        @Schema(description = "Фамилия автора", example = "Doe")
        String authorLastName,

        /**
         * Описание объявления.
         * Пример: В данном объявлении рекламируется корм для животных
         */
        @Schema(description = "Описание объявления", example = "В данном объявлении рекламируется корм для животных")
        String description,

        /**
         * Email автора объявления.
         * Пример: youmail@google.com
         */
        @Schema(description = "E-mail автора", example = "youmail@google.com")
        String email,

        /**
         * Имя файла изображения объявления.
         * Пример: picture.jpg
         */
        @Schema(description = "Файл рисунка объявления", example = "picture.jpg")
        String image,

        /**
         * Телефон автора объявления.
         * Пример: +7499 123123
         */
        @Schema(description = "Телефон пользователя", example = "+7499 123123")
        String phone,

        /**
         * Стоимость объявления.
         * Примечание: в текущем варианте тип String — стоит проверить, не лучше ли использовать числовой тип.
         * Пример: 123
         */
        @Schema(description = "Стоимость объявления", example = "123")
        String price,

        /**
         * Заголовок объявления.
         * Пример: Корм для животных
         */
        @Schema(description = "Заголовок объявления", example = "Корм для животных")
        String title
) {
}
