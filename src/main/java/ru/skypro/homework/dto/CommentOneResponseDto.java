package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для представления одного комментария к объявлению.
 * <p>
 * Используется в ответах контроллера при операциях получения, добавления и обновления комментариев.
 * Содержит данные о самом комментарии (текст, дата), а также сведения об авторе — идентификатор,
 * имя, ссылку на аватар. Дата создания передаётся в формате timestamp (в миллисекундах).
 * </p>
 */
@Schema(description = "DTO одного комментария")
public record CommentOneResponseDto(

        /**
         * Уникальный идентификатор комментария (первичный ключ).
         * Пример: 1
         */
        @Schema(description = "Идентификатор комментария", example = "1")
        Long pk,

        /**
         * Идентификатор автора комментария.
         * Пример: 100
         */
        @Schema(description = "Идентификатор автора", example = "100")
        Long author,

        /**
         * Ссылка на аватар автора комментария.
         * Пример: /images/ad_1.jpg
         */
        @Schema(description = "Ссылка на аватар автора", example = "/images/ad_1.jpg")
        String authorImage,

        /**
         * Имя автора комментария.
         * Пример: Joe
         */
        @Schema(description = "Имя автора", example = "Joe")
        String authorFirstName,

        /**
         * Дата создания комментария в формате Unix‑timestamp (миллисекунды).
         * Пример: 1723132800000
         */
        @Schema(description = "Дата создания комментария в миллисекундах (timestamp)", example = "1723132800000")
        Long createdAt,

        /**
         * Текст комментария.
         * Пример: Мой комментарий к объявлению
         */
        @Schema(description = "Текст комментария", example = "Мой комментарий к объявлению")
        String text
) {
}
