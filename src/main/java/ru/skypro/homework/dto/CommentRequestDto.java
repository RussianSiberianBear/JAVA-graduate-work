package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO одного комментария при создании или обновлении")
public record CommentRequestDto(
        @Schema(description = "Текст комментария",
                example = "Мой комментарий к объявлению",
                minLength = 8,
                maxLength = 64)
        String text
) {
}
