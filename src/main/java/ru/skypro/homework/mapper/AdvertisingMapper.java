package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.skypro.homework.dto.AdvertisingOneResponseDto;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.model.Advertising;

@Mapper(componentModel = "spring")
public interface AdvertisingMapper {

    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "pk", source = "id")
    AdvertisingOneResponseDto toResponse(Advertising ads);

    @Mapping(target = "pk", source = "id")
    @Mapping(target = "authorFirstName", source = "author.firstName")
    @Mapping(target = "authorLastName", source = "author.lastName")
    AdvertisingWithAuthorDto toResponseWithAuthor(Advertising ads);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "image", ignore = true)
    Advertising toEntity(CreateOrUpdateAd createOrUpdateAd);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "image", ignore = true)
    void updateEntity(CreateOrUpdateAd createOrUpdateAd, @MappingTarget Advertising advertising);
}
