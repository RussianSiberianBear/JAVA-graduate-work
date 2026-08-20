package ru.skypro.homework.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
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
import ru.skypro.homework.service.storage.FileReplacementCoordinator;
import ru.skypro.homework.service.storage.FileStorageService;
import ru.skypro.homework.service.storage.FileUploadRequest;
import ru.skypro.homework.service.storage.StoredFileInfo;

import java.io.IOException;
import java.util.List;

/**
 * Основной сервис по работе с объявлениями.
 * <p>
 * Предоставляет методы для создания, обновления, удаления и получения объявлений,
 * а также для проверки прав доступа к ним. Сервис управляет транзакциями и координирует
 * работу с файловым хранилищем, включая замену и удаление файлов.
 * </p>
 */
@Service
@Slf4j
public class AdvertisingService {

    private final AdvertisingRepository advertisingRepository;
    private final AdvertisingMapper advertisingMapper;
    private final UserRepository userRepository;
    private final SecurityHelper securityHelper;
    private final FileStorageService fileService;
    private final FileReplacementCoordinator fileReplacementCoordinator;

    /**
     * Конструктор сервиса.
     *
     * @param advertisingRepository       репозиторий для работы с объявлениями
     * @param advertisingMapper           маппер для преобразования сущностей в DTO и обратно
     * @param userRepository              репозиторий для работы с пользователями
     * @param securityHelper              помощник для работы с безопасностью и данными текущего пользователя
     * @param fileStorageService          сервис для работы с файловым хранилищем
     * @param fileReplacementCoordinator  координатор замены файлов (управление старыми и новыми файлами)
     */
    public AdvertisingService(AdvertisingRepository advertisingRepository,
                              AdvertisingMapper advertisingMapper,
                              UserRepository userRepository,
                              SecurityHelper securityHelper,
                              FileStorageService fileStorageService,
                              FileReplacementCoordinator fileReplacementCoordinator) {
        this.advertisingRepository = advertisingRepository;
        this.advertisingMapper = advertisingMapper;
        this.userRepository = userRepository;
        this.securityHelper = securityHelper;
        this.fileService = fileStorageService;
        this.fileReplacementCoordinator = fileReplacementCoordinator;
    }

    /**
     * Возвращает все имеющиеся объявления.
     * <p>
     * Метод преобразует найденные сущности объявлений в DTO, формирует ответ с общим количеством
     * объявлений и списком DTO. При возникновении ошибки выбрасывается {@link AdvertisingRetrievalException}.
     * </p>
     *
     * @return {@link AdvertisingAllResponseDto} с количеством объявлений и списком их DTO
     * @throws AdvertisingRetrievalException если произошла ошибка при получении объявлений
     */
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

    /**
     * Создаёт новое объявление.
     * <p>
     * Метод находит пользователя по email, загружает файл в хранилище, создаёт сущность объявления,
     * сохраняет её в БД и возвращает DTO созданного объявления. Если при загрузке файла возникает ошибка,
     * сервис пытается откатить загрузку файла. При любых ошибках выбрасывается {@link AdvertisingCreationException}.
     * </p>
     *
     * @param username   email пользователя, создающего объявление
     * @param properties данные для создания/обновления объявления ({@link CreateOrUpdateAd})
     * @param file       загружаемый файл изображения
     * @return DTO созданного объявления ({@link AdvertisingOneResponseDto})
     * @throws IOException                если произошла ошибка ввода-вывода при работе с файлом
     * @throws AdvertisingCreationException если не удалось создать объявление (в т. ч. из-за ошибки хранилища)
     * @throws UsernameNotFoundException  если пользователь с указанным email не найден
     */
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
            if (storedFile != null) {
                try {
                    fileService.delete(storedFile.id());
                    log.info("Rolled back new ads file after FileStorageException: {}", storedFile.id());
                } catch (Exception ex) {
                    log.error("CRITICAL: Failed to rollback new ads file: {}. Manual cleanup required!", storedFile.id(), ex);
                }
            }
            log.error("File storage error while creating ad for user: {}", username, e);
            throw new AdvertisingCreationException("Failed to create ad due to file storage error: " + e.getMessage(), e);

        } catch (Exception e) {
            if (storedFile != null) {
                try {
                    fileService.delete(storedFile.id());
                    log.info("Rolled back new ads file after exception: {}", storedFile.id());
                } catch (Exception ex) {
                    log.error("CRITICAL: Failed to rollback new ads file: {}. Manual cleanup required!", storedFile.id(), ex);
                }
            }
            log.error("Failed to create ad for user: {}", username, e);
            throw new AdvertisingCreationException("Failed to create ad: " + e.getMessage(), e);
        }
    }

    /**
     * Обновляет объявление по его идентификатору.
     * <p>
     * Если объявление не найдено, выбрасывается {@link AdvertisingNotFoundException}.
     * При других ошибках — выбрасывается {@link AdvertisingUpdateException}.
     * </p>
     *
     * @param id         идентификатор объявления
     * @param properties новые данные для обновления объявления
     * @return обновлённое объявление в формате DTO ({@link AdvertisingOneResponseDto})
     * @throws AdvertisingNotFoundException если объявление с указанным ID не найдено
     * @throws AdvertisingUpdateException  если произошла ошибка при обновлении объявления
     */
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

    /**
     * Возвращает все объявления, принадлежащие пользователю с указанным ID.
     *
     * @param userId идентификатор пользователя
     * @return {@link AdvertisingAllResponseDto} с количеством и списком объявлений пользователя
     * @throws AdvertisingRetrievalException если произошла ошибка при поиске объявлений
     */
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

    /**
     * Получает объявление по идентификатору вместе с данными автора.
     *
     * @param id идентификатор объявления
     * @return DTO объявления с информацией об авторе ({@link AdvertisingWithAuthorDto})
     * @throws AdvertisingNotFoundException если объявление не найдено
     * @throws AdvertisingRetrievalException если произошла другая ошибка при получении объявления
     */
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

    /**
     * Удаляет объявление по идентификатору.
     * <p>
     * Удаление файла изображения планируется на момент коммита транзакции через
     * {@link FileReplacementCoordinator#deleteAfterCommit(String)}.
     * </p>
     *
     * @param id идентификатор удаляемого объявления
     * @throws AdvertisingNotFoundException если объявление не найдено
     */
    @Transactional
    public void deleteAdById(Long id) {
        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() ->
                        new AdvertisingNotFoundException(
                                ExceptionMessages.formatAdNotFound(id)
                        )
                );

        String imageFileId = ad.getImageFileId();

        advertisingRepository.delete(ad);

        fileReplacementCoordinator.deleteAfterCommit(imageFileId);

        log.info(
                "Ad scheduled for deletion: {}, imageFileId: {}",
                id,
                imageFileId
        );
    }

    /**
     * Обновляет изображение объявления.
     * <p>
     * Использует координатор замены файлов для загрузки нового изображения и регистрации
     * его как замены старого. Старое изображение не удаляется сразу, а будет обработано
     * координатором в соответствии с его логикой.
     * </p>
     *
     * @param id   идентификатор объявления
     * @param file загружаемый новый файл изображения
     * @throws AdvertisingImageUpdateException если произошла ошибка при чтении или загрузке файла
     * @throws AdvertisingNotFoundException    если объявление не найдено
     */
    @Transactional
    public void updateAdsImage(Long id, MultipartFile file) {
        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() ->
                        new AdvertisingNotFoundException(
                                ExceptionMessages.formatAdNotFound(id)
                        )
                );

        String oldFileId = ad.getImageFileId();

        try {
            FileUploadRequest request = new FileUploadRequest(
                    StorageDirectories.ADS,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream()
            );

            StoredFileInfo newFile =
                    fileReplacementCoordinator.uploadAndRegisterReplacement(
                            request,
                            oldFileId
                    );

            ad.setImageFileId(newFile.id());

            log.info(
                    "Image updated for ad ID: {}, oldFileId: {}, newFileId: {}",
                    id,
                    oldFileId,
                    newFile.id()
            );

        } catch (IOException e) {
            log.error("Failed to read image file for ad ID: {}", id, e);

            throw new AdvertisingImageUpdateException(
                    "Failed to read uploaded ad image: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Проверяет, является ли текущий пользователь автором объявления.
     * <p>
     * Возвращает {@code true}, если автор объявления отличается от текущего пользователя.
     * </p>
     *
     * @param adsId идентификатор объявления
     * @return {@code true} если автор объявления другой пользователь, иначе {@code false}
     * @throws AdvertisingNotFoundException если объявление не найдено
     * @throws AdvertisingRetrievalException если произошла другая ошибка при проверке
     */
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
