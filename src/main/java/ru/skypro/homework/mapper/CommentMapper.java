package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.skypro.homework.dto.CommentOneResponseDto;
import ru.skypro.homework.model.Comment;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(
            target = "image",
            expression = "java(toImageUrl(author.getAvatarFileId()))"
    )
    @Mapping(target = "advertising", ignore = true)
    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "authorFirstName", source = "author.firstName")
    @Mapping(target = "pk", source = "id")
    @Mapping(
            target = "createdAt",
            expression = "java(toTimestamp(comment.getCreatedAt()))"
    )
    CommentOneResponseDto toResponse(Comment comment);

    default String toImageUrl(String fileId) {
        return fileId == null
                ? null
                : "/images/" + fileId;
    }

    // Преобразование LocalDateTime → timestamp (миллисекунды)
    default Long toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
}