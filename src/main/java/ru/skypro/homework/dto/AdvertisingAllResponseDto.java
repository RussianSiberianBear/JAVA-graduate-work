package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Список всех объявлений")
public record AdvertisingAllResponseDto(

        @Schema(description = "Общее количество объявлений", example = "25")
        Long count,

        @Schema(description = "Список объявлений")
        List<AdvertisingOneResponseDto> results

) {}
