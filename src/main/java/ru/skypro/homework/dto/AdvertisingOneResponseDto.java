package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для представления данных одного объявления.
 * <p>
 * Используется в ответах контроллера при операциях получения, создания и обновления объявлений.
 * Содержит ключевые атрибуты объявления: идентификатор, автора, изображение, цену и заголовок.
 * </p>
 */
@Schema(description = "DTO одного объявления")
public record AdvertisingOneResponseDto(

        /**
         * Уникальный идентификатор объявления (первичный ключ).
         * Пример: 1
         */
        @Schema(description = "Идентификатор объявления", example = "1")
        Long pk,

        /**
         * Идентификатор автора объявления.
         * Пример: 100
         */
        @Schema(description = "Идентификатор автора", example = "100")
        Long author,

        /**
         * Ссылка на изображение объявления.
         * Пример: /images/ad_1.jpg
         */
        @Schema(description = "Ссылка на изображение", example = "/images/ad_1.jpg")
        String image,

        /**
         * Цена товара, указанная в объявлении.
         * Пример: 4999
         */
        @Schema(description = "Цена товара", example = "4999")
        Integer price,

        /**
         * Заголовок объявления.
         * Пример: iPhone 15 Pro Max
         */
        @Schema(description = "Заголовок объявления", example = "iPhone 15 Pro Max")
        String title
) {
}
