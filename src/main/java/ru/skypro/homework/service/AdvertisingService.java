package ru.skypro.homework.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.config.StorageDirectories;
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
import java.util.List;

@Service
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

    public Advertising findById(Long id) {
        return advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));
    }

    public AdvertisingAllResponseDto findAll() {
        List<AdvertisingOneResponseDto> adsListDto = advertisingRepository.findAll().stream().map(advertisingMapper::toResponse).toList();
        return new AdvertisingAllResponseDto(adsListDto.size(), adsListDto);
    }

    @Transactional
    public AdvertisingOneResponseDto createAds(String username, CreateOrUpdateAd properties, MultipartFile file) throws IOException {

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionMessages.formatUserNotFound(username)));

        FileUploadRequest fur = new FileUploadRequest(
                StorageDirectories.ADS,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream()
        );

        Advertising advertising = advertisingMapper.toEntity(properties);
        advertising.setAuthor(user);
        StoredFileInfo storedFile = fileService.upload(fur);
        advertising.setImageFileId(storedFile.id());

        Advertising adsSaved = advertisingRepository.save(advertising);
        return advertisingMapper.toResponse(adsSaved);
    }

    @Transactional
    public AdvertisingOneResponseDto updateById(Long id, CreateOrUpdateAd properties) {
        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));

        // Обновляем существующий объект
        advertisingMapper.updateEntity(properties, ad);

        Advertising adsSaved = advertisingRepository.save(ad);
        return advertisingMapper.toResponse(adsSaved);
    }

    public AdvertisingAllResponseDto findAllByUserId(Long userId) {
        List<AdvertisingOneResponseDto> adsListDto = advertisingRepository.findAllByAuthorId(userId).stream().map(advertisingMapper::toResponse).toList();
        return new AdvertisingAllResponseDto(adsListDto.size(), adsListDto);
    }

    public AdvertisingWithAuthorDto getAdById(Long id) {
        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));
        return advertisingMapper.toResponseWithAuthor(ad);
    }

    @Transactional
    public void deleteById(Long id) {
        advertisingRepository.deleteById(id);
    }

    @Transactional
    public void updateAdsImage(Long id, MultipartFile file) {

        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));

        String oldImage = ad.getImageFileId();
        try {
            FileUploadRequest fur = new FileUploadRequest(
                    StorageDirectories.ADS,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream()
            );
            StoredFileInfo storedFile = fileService.replace(oldImage, fur);
            ad.setImageFileId(storedFile.id());
            advertisingRepository.save(ad);
        } catch (Exception e) {
            ad.setImageFileId(oldImage);
            advertisingRepository.save(ad);
        }
    }

    public boolean isAnotherAuthor(Long adsId) {
        Advertising ad = advertisingRepository.findById(adsId)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(adsId)));
        return !ad.getAuthor().getId().equals(securityHelper.getCurrentUserId());
    }
}
