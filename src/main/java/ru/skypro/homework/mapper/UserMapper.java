package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.UserInfoResponseDto;
import ru.skypro.homework.model.User;

/**
 * Маппер по пользователю
 */
@Mapper(componentModel = "spring")
public interface UserMapper extends BaseMapper<User, Register, UserInfoResponseDto> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", source = "username")
    @Mapping(target = "avatarFileId", ignore = true)
    User toEntity(Register request);

    @Override
    @Mapping(
            target = "image",
            expression = "java(toImageUrl(user.getAvatarFileId()))"
    )
    UserInfoResponseDto toResponse(User user);

    default String toImageUrl(String fileId) {
        return fileId == null
                ? null
                : "/images/" + fileId;
    }
}
