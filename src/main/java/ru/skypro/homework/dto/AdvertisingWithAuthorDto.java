package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "DTO одного объявления автора")
public record AdvertisingWithAuthorDto(
        @Schema(description = "Идентификатор объявления", example = "1")
        Long id,
        @Schema(description = "Имя автора", example = "Joe")
        Long authorFirstname,

        @Schema(description = "Фамилия автора", example = "")
        Long authorFirstname
)

{}