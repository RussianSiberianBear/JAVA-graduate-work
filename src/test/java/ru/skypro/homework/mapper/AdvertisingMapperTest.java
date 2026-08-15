package ru.skypro.homework.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.skypro.homework.dto.AdvertisingOneResponseDto;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.model.Advertising;
import ru.skypro.homework.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AdvertisingMapperTest {

    @Autowired
    private AdvertisingMapper advertisingMapper;

    private static final Long AD_ID = 1L;
    private static final Long AUTHOR_ID = 1L;
    private static final String TITLE = "Test Ad";
    private static final String DESCRIPTION = "Test Description";
    private static final Integer PRICE = 100;
    private static final String PRICE_STRING = "100";
    private static final String IMAGE_FILE_ID = "image123";
    private static final String AUTHOR_FIRST_NAME = "John";
    private static final String AUTHOR_LAST_NAME = "Doe";
    private static final String AUTHOR_EMAIL = "john@mail.com";
    private static final String AUTHOR_PHONE = "+79991234567";

    private User createDefaultAuthor() {
        User user = new User();
        user.setId(AUTHOR_ID);
        user.setFirstName(AUTHOR_FIRST_NAME);
        user.setLastName(AUTHOR_LAST_NAME);
        user.setEmail(AUTHOR_EMAIL);
        user.setPhone(AUTHOR_PHONE);
        return user;
    }

    private Advertising createDefaultAdvertising() {
        Advertising ad = new Advertising();
        ad.setId(AD_ID);
        ad.setTitle(TITLE);
        ad.setDescription(DESCRIPTION);
        ad.setPrice(PRICE);
        ad.setImageFileId(IMAGE_FILE_ID);
        ad.setAuthor(createDefaultAuthor());
        return ad;
    }

    @Test
    void toEntity_ShouldMapCreateOrUpdateAdToAdvertising() {
        CreateOrUpdateAd dto = new CreateOrUpdateAd(PRICE, TITLE, DESCRIPTION);

        Advertising result = advertisingMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(TITLE);
        assertThat(result.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(result.getPrice()).isEqualTo(PRICE);
        assertThat(result.getId()).isNull();
        assertThat(result.getImageFileId()).isNull();
        assertThat(result.getAuthor()).isNull();
    }

    @Test
    void toResponse_ShouldMapAdvertisingToResponseDto() {
        Advertising ad = createDefaultAdvertising();

        AdvertisingOneResponseDto result = advertisingMapper.toResponse(ad);

        assertThat(result).isNotNull();
        assertThat(result.pk()).isEqualTo(AD_ID);
        assertThat(result.title()).isEqualTo(TITLE);
        assertThat(result.price()).isEqualTo(PRICE);  // ✅ Integer
        assertThat(result.author()).isEqualTo(AUTHOR_ID);
        assertThat(result.image()).isEqualTo("/images/" + IMAGE_FILE_ID);
    }

    @Test
    void toResponseWithAuthor_ShouldMapAdvertisingToWithAuthorDto() {
        Advertising ad = createDefaultAdvertising();

        AdvertisingWithAuthorDto result = advertisingMapper.toResponseWithAuthor(ad);

        assertThat(result).isNotNull();
        assertThat(result.pk()).isEqualTo(AD_ID);
        assertThat(result.title()).isEqualTo(TITLE);
        assertThat(result.description()).isEqualTo(DESCRIPTION);
        assertThat(result.price()).isEqualTo(PRICE_STRING);  // ✅ String
        assertThat(result.image()).isEqualTo("/images/" + IMAGE_FILE_ID);
        assertThat(result.authorFirstName()).isEqualTo(AUTHOR_FIRST_NAME);
        assertThat(result.authorLastName()).isEqualTo(AUTHOR_LAST_NAME);
        assertThat(result.email()).isEqualTo(AUTHOR_EMAIL);
        assertThat(result.phone()).isEqualTo(AUTHOR_PHONE);
    }

    @Test
    void updateEntity_ShouldUpdateExistingAdvertising() {
        CreateOrUpdateAd dto = new CreateOrUpdateAd(200, "Updated Title", "Updated Description");
        Advertising ad = createDefaultAdvertising();
        ad.setTitle("Old Title");
        ad.setDescription("Old Description");
        ad.setPrice(50);

        advertisingMapper.updateEntity(dto, ad);

        assertThat(ad.getTitle()).isEqualTo("Updated Title");
        assertThat(ad.getDescription()).isEqualTo("Updated Description");
        assertThat(ad.getPrice()).isEqualTo(200);
        assertThat(ad.getId()).isEqualTo(AD_ID);
        assertThat(ad.getImageFileId()).isEqualTo(IMAGE_FILE_ID);
        assertThat(ad.getAuthor()).isNotNull();
    }

    @Test
    void toImageUrl_ShouldConvertFileIdToUrl() {
        String result = advertisingMapper.toImageUrl(IMAGE_FILE_ID);
        assertThat(result).isEqualTo("/images/" + IMAGE_FILE_ID);
    }

    @Test
    void toImageUrl_ShouldReturnNullForNullFileId() {
        String result = advertisingMapper.toImageUrl(null);
        assertThat(result).isNull();
    }
}