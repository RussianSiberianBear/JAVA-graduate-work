package ru.skypro.homework.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.config.StorageDirectories;
import ru.skypro.homework.constants.ExceptionMessages;
import ru.skypro.homework.dto.AdvertisingAllResponseDto;
import ru.skypro.homework.dto.AdvertisingOneResponseDto;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.exception.*;
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
        try {
            List<AdvertisingOneResponseDto> adsListDto = advertisingRepository.findAll()
                    .stream()
                    .map(advertisingMapper::toResponse)
                    .toList();
            return new AdvertisingAllResponseDto(adsListDto.size(), adsListDto);
        } catch (Exception e) {
            log.error("Failed to find all ads", e);
            throw new AdvertisingRetrievalException("Failed to retrieve all ads: " + e.getMessage(), e);
        }
    }

    @Transactional
    public AdvertisingOneResponseDto createAds(String username, CreateOrUpdateAd properties, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionMessages.formatUserNotFound(username)));

        // 1. Сохраняем объявление БЕЗ файла
        Advertising advertising = advertisingMapper.toEntity(properties);
        advertising.setAuthor(user);
        advertising.setImageFileId(null);
        Advertising adsSaved = advertisingRepository.save(advertising);

        StoredFileInfo storedFile = null;
        try {
            // 2. Загружаем файл
            FileUploadRequest fur = new FileUploadRequest(
                    StorageDirectories.ADS,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream()
            );

            storedFile = fileService.upload(fur);

            // 3. Обновляем запись с ID файла
            adsSaved.setImageFileId(storedFile.id());
            Advertising updated = advertisingRepository.save(adsSaved);

            return advertisingMapper.toResponse(updated);

        } catch (Exception e) {
            // Если ошибка - удаляем файл (если загружен)
            if (storedFile != null) {
                try {
                    fileService.delete(storedFile.id());
                    log.info("Rolled back file after error: {}", storedFile.id());
                } catch (Exception ex) {
                    log.error("CRITICAL: Failed to rollback file: {}. Manual cleanup required!", storedFile.id(), ex);
                }
            }

            // Удаляем запись из БД, если она была создана
            if (adsSaved != null && adsSaved.getId() != null) {
                try {
                    advertisingRepository.deleteById(adsSaved.getId());
                    log.info("Rolled back ad record after error: {}", adsSaved.getId());
                } catch (Exception ex) {
                    log.error("CRITICAL: Failed to rollback ad record: {}", adsSaved.getId(), ex);
                }
            }

            log.error("Failed to create ad for user: {}", username, e);
            throw new AdvertisingCreationException("Failed to create ad: " + e.getMessage(), e);
        }
    }

    @Transactional
    public AdvertisingOneResponseDto updateById(Long id, CreateOrUpdateAd properties) {
        try {
            Advertising ad = advertisingRepository.findById(id)
                    .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));

            advertisingMapper.updateEntity(properties, ad);
            Advertising adsSaved = advertisingRepository.save(ad);
            return advertisingMapper.toResponse(adsSaved);

        } catch (AdvertisingNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update ad with ID: {}", id, e);
            throw new AdvertisingUpdateException("Failed to update ad with ID: " + id + ": " + e.getMessage(), e);
        }
    }

    public AdvertisingAllResponseDto findAllByUserId(Long userId) {
        try {
            List<AdvertisingOneResponseDto> adsListDto = advertisingRepository.findAllByAuthorId(userId)
                    .stream()
                    .map(advertisingMapper::toResponse)
                    .toList();
            return new AdvertisingAllResponseDto(adsListDto.size(), adsListDto);
        } catch (Exception e) {
            log.error("Failed to find ads for user ID: {}", userId, e);
            throw new AdvertisingRetrievalException("Failed to retrieve ads for user: " + e.getMessage(), e);
        }
    }

    public AdvertisingWithAuthorDto getAdById(Long id) {
        try {
            Advertising ad = advertisingRepository.findWithAuthorById(id)
                    .orElseThrow(() ->
                            new AdvertisingNotFoundException(
                                    ExceptionMessages.formatAdNotFound(id)
                            )
                    );

            return advertisingMapper.toResponseWithAuthor(ad);

        } catch (AdvertisingNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get ad with ID: {}", id, e);
            throw new AdvertisingRetrievalException(
                    "Failed to retrieve ad with ID: "
                            + id + ": " + e.getMessage(),
                    e
            );
        }
    }

    @Transactional
    public void deleteAdById(Long id) {
        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));

        String imageId = ad.getImageFileId();

        // 1. Сначала удаляем запись из БД
        advertisingRepository.deleteById(id);
        log.info("Successfully deleted ad record with ID: {}", id);

        // 2. ТОЛЬКО ПОСЛЕ успешного удаления из БД удаляем файл
        if (imageId != null && !imageId.isEmpty()) {
            try {
                fileService.delete(imageId);
                log.info("Successfully deleted file for ad ID: {}, fileId: {}", id, imageId);
            } catch (Exception e) {
                // Если не удалось удалить файл - логируем, но не откатываем транзакцию
                // Файл остается как "сирота" для последующей очистки
                log.warn("Failed to delete file for ad ID: {}. File orphaned for manual cleanup. fileId: {}",
                        id, imageId, e);
            }
        }
    }

    @Transactional
    public void updateAdsImage(Long id, MultipartFile file) {
        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));

        String oldImage = ad.getImageFileId();
        StoredFileInfo newFile = null;

        try {
            // 1. Загружаем НОВЫЙ файл
            FileUploadRequest fur = new FileUploadRequest(
                    StorageDirectories.ADS,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream()
            );

            newFile = fileService.replace(oldImage, fur); // Только загружает новый, НЕ удаляет старый

            // 2. Сохраняем новый ID в БД
            ad.setImageFileId(newFile.id());
            advertisingRepository.save(ad);

            // 3. ТОЛЬКО ПОСЛЕ успешного сохранения в БД удаляем старый файл
            if (oldImage != null && !oldImage.isEmpty()) {
                try {
                    fileService.delete(oldImage);
                    log.info("Successfully deleted old file: {}", oldImage);
                } catch (Exception e) {
                    // Если не удалось удалить старый файл - логируем, но не откатываем
                    // Файл остается как "сирота" для последующей очистки
                    log.warn("Failed to delete old file: {}. File orphaned for manual cleanup.", oldImage, e);
                }
            }

            log.info("Successfully updated image for ad ID: {}, oldFileId: {}, newFileId: {}",
                    id, oldImage, newFile.id());

        } catch (Exception e) {
            // Если произошла ошибка - откатываем новый файл
            if (newFile != null) {
                try {
                    fileService.delete(newFile.id());
                    log.info("Rolled back new file after error: {}", newFile.id());
                } catch (Exception ex) {
                    log.error("CRITICAL: Failed to rollback new file: {}. Manual cleanup required!", newFile.id(), ex);
                }
            }

            // НЕ СОХРАНЯЕМ старый ID в БД!
            log.error("Failed to update image for ad ID: {}", id, e);
            throw new AdvertisingImageUpdateException("Failed to update ad image: " + e.getMessage(), e);
        }
    }

    @Transactional
    public boolean isAnotherAuthor(Long adsId) {
        try {
            Advertising ad = advertisingRepository.findById(adsId)
                    .orElseThrow(() ->
                            new AdvertisingNotFoundException(
                                    ExceptionMessages.formatAdNotFound(adsId)
                            )
                    );

            return !ad.getAuthor()
                    .getId()
                    .equals(securityHelper.getCurrentUserId());

        } catch (AdvertisingNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to check author for ad ID: {}", adsId, e);
            throw new AdvertisingRetrievalException(
                    "Failed to check ad author: " + e.getMessage(),
                    e
            );
        }
    }
}