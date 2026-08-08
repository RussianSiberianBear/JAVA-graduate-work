package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.skypro.homework.dto.CommentOneResponseDto;
import ru.skypro.homework.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "advertising", ignore = true)
    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "authorFirstName", source = "author.firstName")
    @Mapping(target = "authorImage", source = "author.image")
    @Mapping(target = "pk", source = "id")
    CommentOneResponseDto toResponse(Comment comment);

}
