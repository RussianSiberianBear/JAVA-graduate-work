package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.dto.CommentOneResponseDto;
import ru.skypro.homework.model.Advertising;
import ru.skypro.homework.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "advertising", ignore = true)
    @Mapping(target = "author", source = "userId")
    @Mapping(target = "authorFirstName", source = "userFirstName")
    @Mapping(target = "authorImage", source = "userImage")
    CommentOneResponseDto toResponse(Comment comment);

}
