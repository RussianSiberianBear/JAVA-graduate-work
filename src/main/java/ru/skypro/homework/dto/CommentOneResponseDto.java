package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO одного комментария")
public record CommentOneResponseDto(
        @Schema(description = "Идентификатор комментария", example = "1")
        Long pk,

        @Schema(description = "Идентификатор автора", example = "100")
        Long author,

        @Schema(description = "Ссылка на аватар автора", example = "/images/ad_1.jpg")
        String authorImage,

        @Schema(description = "Имя автора", example = "Joe")
        String authorFirstName,

        @Schema(description = "Дата создания комментария в миллисекундах time stamp", example = "1723132800000")
        Long createdAt,

        @Schema(description = "Текст комментария", example = "Мой комментарий к объявлению")
        String text
) {
}
