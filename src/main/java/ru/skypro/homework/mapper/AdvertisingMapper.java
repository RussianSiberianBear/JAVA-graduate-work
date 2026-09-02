package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.skypro.homework.dto.AdvertisingOneResponseDto;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.model.Advertising;

/**
 * MapStruct‑маппер для преобразования сущностей и DTO в контексте объявлений.
 * <p>
 * Обеспечивает:
 * - преобразование сущности {@link Advertising} в DTO ответов ({@link AdvertisingOneResponseDto},
 *   {@link AdvertisingWithAuthorDto}) с дополнительной обработкой (например, формированием URL изображения);
 * - создание и обновление сущности {@link Advertising} на основе DTO {@link CreateOrUpdateAd}.
 * </p>
 */
@Mapper(componentModel = "spring", uses = ImageMapperUtil.class)
public interface AdvertisingMapper {

    /**
     * Преобразует сущность объявления в DTO для ответа с базовой информацией.
     * <p>
     * Выполняет следующие маппинги:
     * - image: формирует URL изображения с помощью qualifiedByName = "toImageUrl";
     * - author: маппит ID автора;
     * - pk: маппит ID объявления в поле pk.
     * </p>
     *
     * @param ads сущность объявления
     * @return DTO с базовой информацией об объявлении
     */
    @Mapping(
            target = "image",
            source = "imageFileId",
            qualifiedByName = "toImageUrl"
    )
    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "pk", source = "id")
    AdvertisingOneResponseDto toResponse(Advertising ads);

    /**
     * Преобразует сущность объявления в DTO с развёрнутой информацией об авторе.
     * <p>
     * Выполняет следующие маппинги:
     * - image: формирует URL изображения с помощью qualifiedByName = "toImageUrl";
     * - pk: маппит ID объявления;
     * - поля автора: firstName, lastName, email, phone.
     * </p>
     *
     * @param ads сущность объявления
     * @return DTO с информацией об объявлении и его авторе
     */
    @Mapping(
            target = "image",
            source = "ads.imageFileId",
            qualifiedByName = "toImageUrl"
    )
    @Mapping(target = "pk", source = "id")
    @Mapping(target = "authorFirstName", source = "author.firstName")
    @Mapping(target = "authorLastName", source = "author.lastName")
    @Mapping(target = "email", source = "author.email")
    @Mapping(target = "phone", source = "author.phone")
    AdvertisingWithAuthorDto toResponseWithAuthor(Advertising ads);

    /**
     * Создаёт новую сущность объявления на основе DTO для создания/обновления.
     *
     * @param createOrUpdateAd DTO с данными для создания объявления
     * @return новая сущность {@link Advertising}
     */
    Advertising toEntity(CreateOrUpdateAd createOrUpdateAd);

    /**
     * Обновляет существующую сущность объявления данными из DTO.
     * <p>
     * Использует {@link MappingTarget} для обновления полей уже существующей сущности.
     * </p>
     *
     * @param createOrUpdateAd данные для обновления
     * @param advertising       существующая сущность, которую нужно обновить
     */
    void updateEntity(CreateOrUpdateAd createOrUpdateAd, @MappingTarget Advertising advertising);
}
