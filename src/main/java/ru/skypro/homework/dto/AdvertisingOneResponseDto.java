package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO одного объявления")
public record AdvertisingOneResponseDto(

        @Schema(description = "Идентификатор объявления", example = "1")
        Long pk,

        @Schema(description = "Идентификатор автора", example = "100")
        Long author,

        @Schema(description = "Ссылка на изображение", example = "/images/ad_1.jpg")
        String image,

        @Schema(description = "Цена товара", example = "4999")
        Integer price,

        @Schema(description = "Заголовок объявления", example = "iPhone 15 Pro Max")
        String title

) {
}
