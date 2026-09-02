package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.UserInfoResponseDto;
import ru.skypro.homework.model.User;

/**
 * MapStruct‑маппер для преобразования данных пользователя между DTO и сущностью.
 * <p>
 * Реализует интерфейс {@link BaseMapper}, обеспечивая:
 * - создание сущности User из DTO Register (при регистрации);
 * - преобразование сущности User в DTO UserInfoResponseDto (для ответа API);
 * - формирование URL аватара на основе avatarFileId.
 * </p>
 */
@Mapper(componentModel = "spring", uses = ImageMapperUtil.class)
public interface UserMapper extends BaseMapper<User, Register, UserInfoResponseDto> {

    /**
     * Преобразует DTO регистрации в сущность пользователя.
     * <p>
     * Выполняет следующие маппинги:
     * - email: маппит из поля username (согласно бизнес‑логике проекта);
     * - id, password, avatarFileId: игнорируются (будут заполнены позже, например, при сохранении);
     * - остальные поля (firstName, lastName и т. п.) мапятся автоматически по совпадению имён.
     * </p>
     *
     * @param request DTO с данными для регистрации пользователя
     * @return новая сущность {@link User}
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", source = "username")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "avatarFileId", ignore = true)
    User toEntity(Register request);

    /**
     * Преобразует сущность пользователя в DTO для ответа API.
     * <p>
     * Выполняет следующие маппинги:
     * - image: формирует URL аватара с помощью метода toImageUrl на основе avatarFileId;
     * - остальные поля (id, email, firstName, lastName, phone, role и т. д.) мапятся автоматически.
     * </p>
     *
     * @param user сущность пользователя
     * @return DTO с информацией о пользователе
     */
    @Override
    @Mapping(
            target = "image",
            source = "avatarFileId",
            qualifiedByName = "toImageUrl"
    )
    UserInfoResponseDto toResponse(User user);
}
