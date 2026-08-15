package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
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
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "avatarFileId", ignore = true)
    User toEntity(Register request);

    @Override
    @Mapping(
            target = "image",
            source = "avatarFileId",
            qualifiedByName = "toImageUrl"
    )
    UserInfoResponseDto toResponse(User user);

    @Named("toImageUrl")
    default String toImageUrl(String fileId) {
        return fileId == null
                ? null
                : "/images/" + fileId;
    }
}
