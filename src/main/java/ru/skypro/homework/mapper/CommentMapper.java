package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.skypro.homework.dto.CommentOneResponseDto;
import ru.skypro.homework.model.Comment;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface CommentMapper {

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

    @Named("toImageUrl")
    default String toImageUrl(String fileId) {
        return fileId == null ? null : "/images/" + fileId;
    }

    @Named("toTimestamp")
    default Long toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}