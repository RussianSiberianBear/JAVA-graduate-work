package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO для создания или обновления одного объявления")
public record CreateOrUpdateAd(
        @Schema(description = "Цена товара", example = "4999.99")
        BigDecimal price,

        @Schema(description = "Заголовок объявления", example = "iPhone 15 Pro Max")
        String title,

        @Schema(description = "Подробности объявления", example = "iPhone 15 Pro Max, год выпска..., почти новый...")
        String description
) {
}
