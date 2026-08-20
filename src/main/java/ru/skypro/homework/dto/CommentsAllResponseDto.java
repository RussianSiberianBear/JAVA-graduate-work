package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO для представления списка комментариев с общей статистикой.
 * <p>
 * Используется в ответах контроллера при получении коллекции комментариев
 * к конкретному объявлению. Содержит общее количество комментариев в выборке
 * и сам список комментариев в виде {@link CommentOneResponseDto}.
 * </p>
 */
@Schema(description = "Список всех комментариев")
public record CommentsAllResponseDto(

        /**
         * Общее количество комментариев в выборке.
         * Пример: 25
         */
        @Schema(description = "Общее количество комментариев", example = "25")
        Integer count,

        /**
         * Список комментариев, соответствующих текущей выборке.
         */
        List<CommentOneResponseDto> results
) {
}
