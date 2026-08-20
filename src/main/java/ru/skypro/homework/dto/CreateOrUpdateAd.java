package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для создания или обновления объявления.
 * <p>
 * Используется в запросах к контроллеру при добавлении нового объявления
 * или редактировании существующего. Содержит основные поля объявления:
 * цену (с ограничениями от 1 до 10 000 000), заголовок (от 4 до 32 символов)
 * и описание (от 8 до 64 символов).
 * </p>
 */
@Schema(description = "DTO для создания или обновления одного объявления")
public record CreateOrUpdateAd(

        /**
         * Цена товара в объявлении.
         * Допустимый диапазон: от 1 до 10 000 000.
         * Пример: 4999
         */
        @Schema(description = "Цена товара", example = "4999",
                minimum = "1", maximum = "10000000")
        Integer price,

        /**
         * Заголовок объявления.
         * Длина: от 4 до 32 символов.
         * Пример: iPhone 15 Pro Max
         */
        @Schema(description = "Заголовок объявления", example = "iPhone 15 Pro Max",
                minLength = 4,
                maxLength = 32)
        String title,

        /**
         * Подробности объявления.
         * Длина: от 8 до 64 символов.
         * Пример: iPhone 15 Pro Max, год выпуска..., почти новый...
         */
        @Schema(description = "Подробности объявления", example = "iPhone 15 Pro Max, год выпска..., почти новый...",
                minLength = 8,
                maxLength = 64)
        String description
) {
}
