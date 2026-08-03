package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.skypro.homework.dto.AdvertisingOneResponseDto;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.model.Advertising;

@Mapper(componentModel = "spring")
public interface AdvertisingMapper {

    @Mapping(target = "author", source = "userId")
    AdvertisingOneResponseDto toResponse(Advertising ads);

    @Mapping(target = "authorFirstName", source = "author.firstName")
    @Mapping(target = "authorLastName", source = "author.lastName")
    AdvertisingWithAuthorDto toResponseWithAuthor(Advertising ads);

}
