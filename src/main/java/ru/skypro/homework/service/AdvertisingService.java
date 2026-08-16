package ru.skypro.homework.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.config.StorageDirectories;
import ru.skypro.homework.constants.ExceptionMessages;
import ru.skypro.homework.dto.AdvertisingAllResponseDto;
import ru.skypro.homework.dto.AdvertisingOneResponseDto;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.exception.AdvertisingNotFoundException;
import ru.skypro.homework.exception.FileStorageException;
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
import java.util.List;

@Service
@Slf4j
public class AdvertisingService {

    private final AdvertisingRepository advertisingRepository;
    private final AdvertisingMapper advertisingMapper;
    private final UserRepository userRepository;
    private final SecurityHelper securityHelper;
    private final FileStorageService fileService;

    public AdvertisingService(AdvertisingRepository advertisingRepository,
                              AdvertisingMapper advertisingMapper,
                              UserRepository userRepository,
                              SecurityHelper securityHelper,
                              FileStorageService fileStorageService) {
        this.advertisingRepository = advertisingRepository;
        this.advertisingMapper = advertisingMapper;
        this.userRepository = userRepository;
        this.securityHelper = securityHelper;
        this.fileService = fileStorageService;
    }

    public AdvertisingAllResponseDto findAll() {
        List<AdvertisingOneResponseDto> adsListDto = advertisingRepository.findAll()
                .stream()
                .map(advertisingMapper::toResponse)
                .toList();
        return new AdvertisingAllResponseDto(adsListDto.size(), adsListDto);
    }

    @Transactional
    public AdvertisingOneResponseDto createAds(String username, CreateOrUpdateAd properties, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionMessages.formatUserNotFound(username)));

        StoredFileInfo storedFile = null;
        try {
            FileUploadRequest fur = new FileUploadRequest(
                    StorageDirectories.ADS,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream()
            );

            Advertising advertising = advertisingMapper.toEntity(properties);
            advertising.setAuthor(user);
            storedFile = fileService.upload(fur);
            advertising.setImageFileId(storedFile.id());

            Advertising adsSaved = advertisingRepository.save(advertising);
            return advertisingMapper.toResponse(adsSaved);

        } catch (FileStorageException e) {
            // Ошибка при загрузке файла - откатываем файл если он был создан
            if (storedFile != null) {
                try {
                    fileService.delete(storedFile.id());
                    log.info("Rolled back new ads file after FileStorageException: {}", storedFile.id());
                } catch (Exception ex) {
                    log.error("CRITICAL: Failed to rollback new ads file: {}. Manual cleanup required!", storedFile.id(), ex);
                }
            }
            log.error("File storage error while creating ad for user: {}", username, e);
            throw new RuntimeException("Failed to create ad due to file storage error: " + e.getMessage(), e);

        } catch (Exception e) {
            // Другие ошибки (например, ошибка БД)
            if (storedFile != null) {
                try {
                    fileService.delete(storedFile.id());
                    log.info("Rolled back new ads file after exception: {}", storedFile.id());
                } catch (Exception ex) {
                    log.error("CRITICAL: Failed to rollback new ads file: {}. Manual cleanup required!", storedFile.id(), ex);
                }
            }
            log.error("Failed to create ad for user: {}", username, e);
            throw e;
        }
    }

    @Transactional
    public AdvertisingOneResponseDto updateById(Long id, CreateOrUpdateAd properties) {
        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));

        advertisingMapper.updateEntity(properties, ad);
        Advertising adsSaved = advertisingRepository.save(ad);
        return advertisingMapper.toResponse(adsSaved);
    }

    public AdvertisingAllResponseDto findAllByUserId(Long userId) {
        List<AdvertisingOneResponseDto> adsListDto = advertisingRepository.findAllByAuthorId(userId)
                .stream()
                .map(advertisingMapper::toResponse)
                .toList();
        return new AdvertisingAllResponseDto(adsListDto.size(), adsListDto);
    }

    public AdvertisingWithAuthorDto getAdById(Long id) {
        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));
        return advertisingMapper.toResponseWithAuthor(ad);
    }

    @Transactional
    public void deleteAdById(Long id) {
        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));

        String imageId = ad.getImageFileId();

        try {
            // Сначала удаляем файл, потом запись в БД
            if (imageId != null && !imageId.isEmpty()) {
                fileService.delete(imageId);
                log.info("Successfully deleted file for ad ID: {}, fileId: {}", id, imageId);
            }
            advertisingRepository.deleteById(id);
            log.info("Successfully deleted ad with ID: {}", id);

        } catch (FileStorageException e) {
            log.error("File storage error while deleting ad with ID: {} and imageId: {}. Manual cleanup required!", id, imageId, e);
            throw new RuntimeException("Failed to delete ad due to file storage error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while deleting ad with ID: {} and imageId: {}", id, imageId, e);
            throw e;
        }
    }

    @Transactional
    public void updateAdsImage(Long id, MultipartFile file) {
        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));

        String oldImage = ad.getImageFileId();
        StoredFileInfo storedFile = null;

        try {
            FileUploadRequest fur = new FileUploadRequest(
                    StorageDirectories.ADS,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream()
            );

            // Заменяем файл - внутри replace() старый файл удаляется
            storedFile = fileService.replace(oldImage, fur);
            ad.setImageFileId(storedFile.id());
            advertisingRepository.save(ad);

            log.info("Successfully updated image for ad ID: {}, oldFileId: {}, newFileId: {}",
                    id, oldImage, storedFile.id());

        } catch (FileStorageException e) {
            // Ошибка при работе с файловым хранилищем
            String newFileId = storedFile != null ? storedFile.id() : "null";
            log.error("File storage error while updating image for ad ID: {}. New fileId: {}, Old fileId: {}",
                    id, newFileId, oldImage, e);

            // Возвращаем старый ID в БД
            ad.setImageFileId(oldImage);
            advertisingRepository.save(ad);

            throw new RuntimeException("Failed to update ad image due to file storage error: " + e.getMessage(), e);

        } catch (IOException e) {
            // Ошибка при чтении файла
            String newFileId = storedFile != null ? storedFile.id() : "null";
            log.error("IO error while updating image for ad ID: {}. New fileId: {}", id, newFileId, e);

            // Возвращаем старый ID в БД
            ad.setImageFileId(oldImage);
            advertisingRepository.save(ad);

            throw new RuntimeException("Failed to read file content for ad image update: " + e.getMessage(), e);

        } catch (Exception e) {
            // Другие ошибки
            String newFileId = storedFile != null ? storedFile.id() : "null";
            log.error("Unexpected error while updating image for ad ID: {}. New fileId: {}", id, newFileId, e);

            // Возвращаем старый ID в БД
            ad.setImageFileId(oldImage);
            advertisingRepository.save(ad);

            throw e;
        }
    }

    public boolean isAnotherAuthor(Long adsId) {
        Advertising ad = advertisingRepository.findById(adsId)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(adsId)));
        return !ad.getAuthor().getId().equals(securityHelper.getCurrentUserId());
    }
}