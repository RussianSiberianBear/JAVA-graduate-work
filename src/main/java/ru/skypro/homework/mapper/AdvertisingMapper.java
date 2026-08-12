package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.skypro.homework.dto.AdvertisingOneResponseDto;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.model.Advertising;

@Mapper(componentModel = "spring")
public interface AdvertisingMapper {

    @Mapping(
            target = "image",
            expression = "java(toImageUrl(ads.getImageFileId()))"
    )
    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "pk", source = "id")
    AdvertisingOneResponseDto toResponse(Advertising ads);

    @Mapping(
            target = "image",
            source = "ads.imageFileId",
            qualifiedByName = "toImageUrl"
    )
    @Mapping(target = "pk", source = "id")
    @Mapping(target = "authorFirstName", source = "author.firstName")
    @Mapping(target = "authorLastName", source = "author.lastName")
    AdvertisingWithAuthorDto toResponseWithAuthor(Advertising ads);

    Advertising toEntity(CreateOrUpdateAd createOrUpdateAd);

    void updateEntity(CreateOrUpdateAd createOrUpdateAd, @MappingTarget Advertising advertising);

    @Named("toImageUrl")
    default String toImageUrl(String fileId) {
        return fileId == null
                ? null
                : "/images/" + fileId;
    }
}
