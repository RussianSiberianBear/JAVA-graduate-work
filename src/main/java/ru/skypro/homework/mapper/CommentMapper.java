package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.skypro.homework.dto.CommentOneResponseDto;
import ru.skypro.homework.model.Comment;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * MapStruct‑маппер для преобразования сущности комментария в DTO ответа.
 * <p>
 * Обеспечивает:
 * - маппинг идентификаторов и данных автора (ID, имя, аватар);
 * - преобразование даты создания комментария из LocalDateTime в Unix‑timestamp (в миллисекундах);
 * - формирование URL для аватара автора по идентификатору файла.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface CommentMapper {

    /**
     * Преобразует сущность комментария в DTO для ответа.
     * <p>
     * Выполняет следующие маппинги:
     * - pk: маппит ID комментария в поле pk;
     * - author: маппит ID автора;
     * - authorFirstName: маппит имя автора;
     * - authorImage: формирует URL аватара с помощью метода toImageUrl;
     * - createdAt: преобразует LocalDateTime в timestamp (в мс) с помощью метода toTimestamp.
     * </p>
     *
     * @param comment сущность комментария
     * @return DTO с информацией о комментарии
     */
    @Mapping(target = "pk", source = "id")
    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "authorFirstName", source = "author.firstName")
    @Mapping(
            target = "authorImage",
            source = "comment.author.avatarFileId",
            qualifiedByName = "toImageUrl"
    )
    @Mapping(
            target = "createdAt",
            source = "createdAt",
            qualifiedByName = "toTimestamp"
    )
    CommentOneResponseDto toResponse(Comment comment);

    /**
     * Вспомогательный метод для формирования URL изображения по идентификатору файла.
     * <p>
     * Если fileId равен null, возвращает null. Иначе формирует путь вида "/images/{fileId}".
     * Используется в маппере через аннотацию @Named("toImageUrl") и qualifiedByName.
     * </p>
     *
     * @param fileId идентификатор файла изображения
     * @return сформированный URL изображения или null, если идентификатор отсутствует
     */
    @Named("toImageUrl")
    default String toImageUrl(String fileId) {
        return fileId == null ? null : "/images/" + fileId;
    }

    /**
     * Вспомогательный метод для преобразования LocalDateTime в Unix‑timestamp в миллисекундах.
     * <p>
     * Использует системный часовой пояс (ZoneId.systemDefault()). Если дата равна null,
     * возвращает null. Применяется через @Named("toTimestamp") и qualifiedByName для поля createdAt.
     * </p>
     *
     * @param dateTime дата и время в формате LocalDateTime
     * @return timestamp в миллисекундах или null, если дата отсутствует
     */
    @Named("toTimestamp")
    default Long toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
