package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Список всех комментариев")
public record CommentsAllResponseDto(
        @Schema(description = "Общее количество комментариев", example = "25")
        Integer count,

        List<CommentOneResponseDto> results
) {
}
