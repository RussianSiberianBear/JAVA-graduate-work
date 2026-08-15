package ru.skypro.homework.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.constants.ExceptionMessages;
import ru.skypro.homework.dto.AdvertisingAllResponseDto;
import ru.skypro.homework.dto.AdvertisingOneResponseDto;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.exception.AdvertisingNotFoundException;
import ru.skypro.homework.exception.UsernameNotFoundException;
import ru.skypro.homework.mapper.AdvertisingMapper;
import ru.skypro.homework.model.Advertising;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.AdvertisingRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.security.SecurityHelper;
import ru.skypro.homework.service.storage.FileStorageService;
import ru.skypro.homework.service.storage.FileUploadRequest;
import ru.skypro.homework.service.storage.StoredFileInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisingServiceTest {

    // ===== КОНСТАНТЫ =====
    private static final Long AD_ID = 1L;
    private static final Long NON_EXISTENT_AD_ID = 999L;
    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "user@mail.com";
    private static final String USER_FIRST_NAME = "John";
    private static final String USER_LAST_NAME = "Doe";
    private static final String USER_PHONE = "+79991234567";
    private static final String AD_TITLE = "Test Ad";
    private static final String AD_DESCRIPTION = "Test Description";
    private static final Integer AD_PRICE = 100;
    private static final String IMAGE_FILE_ID = "image123";
    private static final String NEW_IMAGE_FILE_ID = "image456";
    private static final String FILE_NAME = "image.jpg";
    private static final String FILE_CONTENT_TYPE = "image/jpeg";
    private static final long FILE_SIZE = 1024L;

    @Mock
    private AdvertisingRepository advertisingRepository;

    @Mock
    private AdvertisingMapper advertisingMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private AdvertisingService advertisingService;

    // ===== МЕТОДЫ-ФАБРИКИ =====

    private User createDefaultUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        user.setPhone(USER_PHONE);
        return user;
    }

    private Advertising createDefaultAdvertising() {
        Advertising ad = new Advertising();
        ad.setId(AD_ID);
        ad.setTitle(AD_TITLE);
        ad.setDescription(AD_DESCRIPTION);
        ad.setPrice(AD_PRICE);
        ad.setImageFileId(IMAGE_FILE_ID);
        ad.setAuthor(createDefaultUser());
        return ad;
    }

    private CreateOrUpdateAd createDefaultCreateOrUpdateAd() {
        return new CreateOrUpdateAd(AD_PRICE, AD_TITLE, AD_DESCRIPTION);
    }

    private AdvertisingOneResponseDto createDefaultAdvertisingOneResponseDto() {
        return new AdvertisingOneResponseDto(
                AD_ID,
                USER_ID,
                "/images/ads/image.jpg",
                AD_PRICE,
                AD_TITLE
        );
    }

    private AdvertisingWithAuthorDto createDefaultAdvertisingWithAuthorDto() {
        return new AdvertisingWithAuthorDto(
                AD_ID,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                AD_DESCRIPTION,
                USER_EMAIL,
                "/images/ads/image.jpg",
                USER_PHONE,
                IMAGE_FILE_ID,
                AD_TITLE
        );
    }

    private StoredFileInfo createDefaultStoredFileInfo() {
        return new StoredFileInfo(IMAGE_FILE_ID, FILE_NAME, FILE_CONTENT_TYPE, FILE_SIZE);
    }

    private StoredFileInfo createNewStoredFileInfo() {
        return new StoredFileInfo(NEW_IMAGE_FILE_ID, "new_image.jpg", FILE_CONTENT_TYPE, FILE_SIZE);
    }

    // ===== ТЕСТЫ ДЛЯ findAll =====

    @Test
    void findAll_Success_Test() {
        Advertising ad1 = createDefaultAdvertising();
        Advertising ad2 = createDefaultAdvertising();
        ad2.setId(2L);
        ad2.setTitle("Test Ad 2");

        List<Advertising> ads = Arrays.asList(ad1, ad2);

        AdvertisingOneResponseDto dto1 = createDefaultAdvertisingOneResponseDto();
        AdvertisingOneResponseDto dto2 = new AdvertisingOneResponseDto(
                2L, USER_ID, "/images/ads/image2.jpg", 200, "Test Ad 2"
        );

        when(advertisingRepository.findAll()).thenReturn(ads);
        when(advertisingMapper.toResponse(ad1)).thenReturn(dto1);
        when(advertisingMapper.toResponse(ad2)).thenReturn(dto2);

        AdvertisingAllResponseDto result = advertisingService.findAll();

        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(2);
        assertThat(result.results()).hasSize(2);

        verify(advertisingRepository, times(1)).findAll();
        verify(advertisingMapper, times(2)).toResponse(any(Advertising.class));
    }

    @Test
    void findAll_EmptyList_Test() {
        when(advertisingRepository.findAll()).thenReturn(List.of());

        AdvertisingAllResponseDto result = advertisingService.findAll();

        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(0);
        assertThat(result.results()).isEmpty();

        verify(advertisingRepository, times(1)).findAll();
        verify(advertisingMapper, never()).toResponse(any());
    }

    // ===== ТЕСТЫ ДЛЯ createAds =====

    @Test
    void createAds_Success_Test() throws IOException {
        CreateOrUpdateAd properties = createDefaultCreateOrUpdateAd();
        User user = createDefaultUser();
        Advertising ad = createDefaultAdvertising();
        AdvertisingOneResponseDto expected = createDefaultAdvertisingOneResponseDto();
        StoredFileInfo storedFileInfo = createDefaultStoredFileInfo();

        when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
        when(multipartFile.getContentType()).thenReturn(FILE_CONTENT_TYPE);
        when(multipartFile.getSize()).thenReturn(FILE_SIZE);
        when(multipartFile.getInputStream()).thenReturn(mock(InputStream.class));

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(advertisingMapper.toEntity(properties)).thenReturn(ad);
        when(fileStorageService.upload(any(FileUploadRequest.class))).thenReturn(storedFileInfo);
        when(advertisingRepository.save(ad)).thenReturn(ad);
        when(advertisingMapper.toResponse(ad)).thenReturn(expected);

        AdvertisingOneResponseDto result = advertisingService.createAds(USER_EMAIL, properties, multipartFile);

        assertThat(result).isNotNull();
        assertThat(result.pk()).isEqualTo(AD_ID);

        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(advertisingMapper, times(1)).toEntity(properties);
        verify(fileStorageService, times(1)).upload(any(FileUploadRequest.class));
        verify(advertisingRepository, times(1)).save(ad);
        verify(advertisingMapper, times(1)).toResponse(ad);
    }

    @Test
    void createAds_UserNotFound_Test() throws IOException {
        CreateOrUpdateAd properties = createDefaultCreateOrUpdateAd();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advertisingService.createAds(USER_EMAIL, properties, multipartFile))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(ExceptionMessages.formatUserNotFound(USER_EMAIL));

        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(advertisingMapper, never()).toEntity(any());
        verify(fileStorageService, never()).upload(any());
        verify(advertisingRepository, never()).save(any());
    }

    @Test
    void createAds_WhenFileStorageThrowsRuntimeException_Test() throws IOException {
        CreateOrUpdateAd properties = createDefaultCreateOrUpdateAd();
        User user = createDefaultUser();
        Advertising ad = createDefaultAdvertising();

        when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
        when(multipartFile.getContentType()).thenReturn(FILE_CONTENT_TYPE);
        when(multipartFile.getSize()).thenReturn(FILE_SIZE);
        when(multipartFile.getInputStream()).thenReturn(mock(InputStream.class));

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(advertisingMapper.toEntity(properties)).thenReturn(ad);
        when(fileStorageService.upload(any(FileUploadRequest.class)))
                .thenThrow(new RuntimeException("File upload failed"));

        assertThatThrownBy(() -> advertisingService.createAds(USER_EMAIL, properties, multipartFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("File upload failed");

        verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        verify(advertisingMapper, times(1)).toEntity(properties);
        verify(fileStorageService, times(1)).upload(any(FileUploadRequest.class));
        verify(advertisingRepository, never()).save(ad);
    }

    // ===== ТЕСТЫ ДЛЯ updateById =====

    @Test
    void updateById_Success_Test() {
        Long adId = AD_ID;
        CreateOrUpdateAd properties = createDefaultCreateOrUpdateAd();
        Advertising existingAd = createDefaultAdvertising();
        AdvertisingOneResponseDto expected = createDefaultAdvertisingOneResponseDto();

        when(advertisingRepository.findById(adId)).thenReturn(Optional.of(existingAd));
        when(advertisingRepository.save(existingAd)).thenReturn(existingAd);
        when(advertisingMapper.toResponse(existingAd)).thenReturn(expected);

        AdvertisingOneResponseDto result = advertisingService.updateById(adId, properties);

        assertThat(result).isNotNull();
        assertThat(result.pk()).isEqualTo(AD_ID);

        verify(advertisingRepository, times(1)).findById(adId);
        verify(advertisingMapper, times(1)).updateEntity(properties, existingAd);
        verify(advertisingRepository, times(1)).save(existingAd);
        verify(advertisingMapper, times(1)).toResponse(existingAd);
    }

    @Test
    void updateById_AdNotFound_Test() {
        Long adId = NON_EXISTENT_AD_ID;
        CreateOrUpdateAd properties = createDefaultCreateOrUpdateAd();

        when(advertisingRepository.findById(adId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advertisingService.updateById(adId, properties))
                .isInstanceOf(AdvertisingNotFoundException.class)
                .hasMessage(ExceptionMessages.formatAdNotFound(adId));

        verify(advertisingRepository, times(1)).findById(adId);
        verify(advertisingMapper, never()).updateEntity(any(), any());
        verify(advertisingRepository, never()).save(any());
    }

    // ===== ТЕСТЫ ДЛЯ findAllByUserId =====

    @Test
    void findAllByUserId_Success_Test() {
        Long userId = USER_ID;
        Advertising ad1 = createDefaultAdvertising();
        Advertising ad2 = createDefaultAdvertising();
        ad2.setId(2L);

        List<Advertising> ads = Arrays.asList(ad1, ad2);

        AdvertisingOneResponseDto dto1 = createDefaultAdvertisingOneResponseDto();
        AdvertisingOneResponseDto dto2 = new AdvertisingOneResponseDto(
                2L, userId, "/images/ads/image2.jpg", 200, "Test Ad 2"
        );

        when(advertisingRepository.findAllByAuthorId(userId)).thenReturn(ads);
        when(advertisingMapper.toResponse(ad1)).thenReturn(dto1);
        when(advertisingMapper.toResponse(ad2)).thenReturn(dto2);

        AdvertisingAllResponseDto result = advertisingService.findAllByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(2);
        assertThat(result.results()).hasSize(2);

        verify(advertisingRepository, times(1)).findAllByAuthorId(userId);
        verify(advertisingMapper, times(2)).toResponse(any(Advertising.class));
    }

    @Test
    void findAllByUserId_EmptyList_Test() {
        Long userId = USER_ID;

        when(advertisingRepository.findAllByAuthorId(userId)).thenReturn(List.of());

        AdvertisingAllResponseDto result = advertisingService.findAllByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(0);
        assertThat(result.results()).isEmpty();

        verify(advertisingRepository, times(1)).findAllByAuthorId(userId);
        verify(advertisingMapper, never()).toResponse(any());
    }

    // ===== ТЕСТЫ ДЛЯ getAdById =====

    @Test
    void getAdById_Success_Test() {
        Long adId = AD_ID;
        Advertising ad = createDefaultAdvertising();
        AdvertisingWithAuthorDto expected = createDefaultAdvertisingWithAuthorDto();

        when(advertisingRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(advertisingMapper.toResponseWithAuthor(ad)).thenReturn(expected);

        AdvertisingWithAuthorDto result = advertisingService.getAdById(adId);

        assertThat(result).isNotNull();
        assertThat(result.pk()).isEqualTo(AD_ID);

        verify(advertisingRepository, times(1)).findById(adId);
        verify(advertisingMapper, times(1)).toResponseWithAuthor(ad);
    }

    @Test
    void getAdById_AdNotFound_Test() {
        Long adId = NON_EXISTENT_AD_ID;

        when(advertisingRepository.findById(adId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advertisingService.getAdById(adId))
                .isInstanceOf(AdvertisingNotFoundException.class)
                .hasMessage(ExceptionMessages.formatAdNotFound(adId));

        verify(advertisingRepository, times(1)).findById(adId);
        verify(advertisingMapper, never()).toResponseWithAuthor(any());
    }

    // ===== ТЕСТЫ ДЛЯ deleteAdById =====

    @Test
    void deleteAdById_Success_Test() {
        Long adId = AD_ID;

        doNothing().when(advertisingRepository).deleteById(adId);

        advertisingService.deleteAdById(adId);

        verify(advertisingRepository, times(1)).deleteById(adId);
    }

    // ===== ТЕСТЫ ДЛЯ updateAdsImage =====

    @Test
    void updateAdsImage_Success_Test() throws IOException {
        Long adId = AD_ID;
        Advertising ad = createDefaultAdvertising();
        StoredFileInfo newStoredFileInfo = createNewStoredFileInfo();

        when(advertisingRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(multipartFile.getOriginalFilename()).thenReturn("new_image.jpg");
        when(multipartFile.getContentType()).thenReturn(FILE_CONTENT_TYPE);
        when(multipartFile.getSize()).thenReturn(FILE_SIZE);
        when(multipartFile.getInputStream()).thenReturn(mock(InputStream.class));
        when(fileStorageService.replace(eq(IMAGE_FILE_ID), any(FileUploadRequest.class)))
                .thenReturn(newStoredFileInfo);
        when(advertisingRepository.save(ad)).thenReturn(ad);

        advertisingService.updateAdsImage(adId, multipartFile);

        verify(advertisingRepository, times(1)).findById(adId);
        verify(fileStorageService, times(1)).replace(eq(IMAGE_FILE_ID), any(FileUploadRequest.class));
        verify(advertisingRepository, times(1)).save(ad);
        assertThat(ad.getImageFileId()).isEqualTo(NEW_IMAGE_FILE_ID);
    }

    @Test
    void updateAdsImage_AdNotFound_Test() {
        Long adId = NON_EXISTENT_AD_ID;

        when(advertisingRepository.findById(adId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advertisingService.updateAdsImage(adId, multipartFile))
                .isInstanceOf(AdvertisingNotFoundException.class)
                .hasMessage(ExceptionMessages.formatAdNotFound(adId));

        verify(advertisingRepository, times(1)).findById(adId);
        verify(fileStorageService, never()).replace(anyString(), any());
        verify(advertisingRepository, never()).save(any());
    }

    @Test
    void updateAdsImage_WhenReplaceThrowsException_ShouldRestoreOldImage_Test() throws IOException {
        Long adId = AD_ID;
        Advertising ad = createDefaultAdvertising();
        String oldImageId = ad.getImageFileId();

        when(advertisingRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(multipartFile.getOriginalFilename()).thenReturn("new_image.jpg");
        when(multipartFile.getContentType()).thenReturn(FILE_CONTENT_TYPE);
        when(multipartFile.getSize()).thenReturn(FILE_SIZE);
        when(multipartFile.getInputStream()).thenReturn(mock(InputStream.class));
        when(fileStorageService.replace(eq(IMAGE_FILE_ID), any(FileUploadRequest.class)))
                .thenThrow(new RuntimeException("Replace failed"));
        when(advertisingRepository.save(ad)).thenReturn(ad);

        advertisingService.updateAdsImage(adId, multipartFile);

        verify(advertisingRepository, times(1)).findById(adId);
        verify(fileStorageService, times(1)).replace(eq(IMAGE_FILE_ID), any(FileUploadRequest.class));
        // В методе save вызывается 1 раз (после восстановления старого imageId)
        verify(advertisingRepository, times(1)).save(ad);
        assertThat(ad.getImageFileId()).isEqualTo(oldImageId);
    }

    @Test
    void updateAdsImage_WhenFileStorageThrowsRuntimeException_Test() throws IOException {
        Long adId = AD_ID;
        Advertising ad = createDefaultAdvertising();

        when(advertisingRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(multipartFile.getOriginalFilename()).thenReturn("new_image.jpg");
        when(multipartFile.getContentType()).thenReturn(FILE_CONTENT_TYPE);
        when(multipartFile.getSize()).thenReturn(FILE_SIZE);
        when(multipartFile.getInputStream()).thenReturn(mock(InputStream.class));
        when(fileStorageService.replace(eq(IMAGE_FILE_ID), any(FileUploadRequest.class)))
                .thenThrow(new RuntimeException("File storage error"));

        advertisingService.updateAdsImage(adId, multipartFile);

        verify(advertisingRepository, times(1)).findById(adId);
        verify(fileStorageService, times(1)).replace(eq(IMAGE_FILE_ID), any(FileUploadRequest.class));
        verify(advertisingRepository, times(1)).save(ad);
        assertThat(ad.getImageFileId()).isEqualTo(IMAGE_FILE_ID);
    }

    // ===== ТЕСТЫ ДЛЯ isAnotherAuthor =====

    @Test
    void isAnotherAuthor_WhenUserIsNotAuthor_Test() {
        Long adId = AD_ID;
        Advertising ad = createDefaultAdvertising();
        Long currentUserId = 999L;

        when(advertisingRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(securityHelper.getCurrentUserId()).thenReturn(currentUserId);

        boolean result = advertisingService.isAnotherAuthor(adId);

        assertThat(result).isTrue();
        verify(advertisingRepository, times(1)).findById(adId);
        verify(securityHelper, times(1)).getCurrentUserId();
    }

    @Test
    void isAnotherAuthor_WhenUserIsAuthor_Test() {
        Long adId = AD_ID;
        Advertising ad = createDefaultAdvertising();

        when(advertisingRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(securityHelper.getCurrentUserId()).thenReturn(USER_ID);

        boolean result = advertisingService.isAnotherAuthor(adId);

        assertThat(result).isFalse();
        verify(advertisingRepository, times(1)).findById(adId);
        verify(securityHelper, times(1)).getCurrentUserId();
    }

    @Test
    void isAnotherAuthor_AdNotFound_Test() {
        Long adId = NON_EXISTENT_AD_ID;

        when(advertisingRepository.findById(adId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advertisingService.isAnotherAuthor(adId))
                .isInstanceOf(AdvertisingNotFoundException.class)
                .hasMessage(ExceptionMessages.formatAdNotFound(adId));

        verify(advertisingRepository, times(1)).findById(adId);
        verify(securityHelper, never()).getCurrentUserId();
    }
}