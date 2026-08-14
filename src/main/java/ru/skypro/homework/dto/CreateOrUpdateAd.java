package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO для создания или обновления одного объявления")
public record CreateOrUpdateAd(
        @Schema(description = "Цена товара", example = "4999",
                minimum = "1", maximum = "10000000")
        Integer price,

        @Schema(description = "Заголовок объявления", example = "iPhone 15 Pro Max",
                minLength = 4,
                maxLength = 32)
        String title,

        @Schema(description = "Подробности объявления", example = "iPhone 15 Pro Max, год выпска..., почти новый...",
                minLength = 8,
                maxLength = 64)
        String description
) {
}
