package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO для представления списка объявлений с общей статистикой.
 * <p>
 * Используется в ответах контроллера при получении коллекции объявлений.
 * Содержит общее количество записей и сам список объявлений в виде {@link AdvertisingOneResponseDto}.
 * </p>
 */
@Schema(description = "Список всех объявлений")
public record AdvertisingAllResponseDto(

        /**
         * Общее количество объявлений в выборке.
         * Пример: 25
         */
        @Schema(description = "Общее количество объявлений", example = "25")
        Integer count,

        /**
         * Список объявлений, соответствующих текущей выборке.
         */
        @Schema(description = "Список объявлений")
        List<AdvertisingOneResponseDto> results
) {
}
