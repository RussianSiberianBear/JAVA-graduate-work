package ru.skypro.homework.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
import ru.skypro.homework.util.SecurityHelper;

import java.util.List;

@Service
public class AdvertisingService {

    private final AdvertisingRepository advertisingRepository;
    private final AdvertisingMapper advertisingMapper;
    private final UserRepository userRepository;
    private final AdvertisingPrice advertisingPrice;
    private final SecurityHelper securityHelper;

    public AdvertisingService(AdvertisingRepository advertisingRepository, AdvertisingMapper advertisingMapper, UserRepository userRepository, AdvertisingPrice advertisingPrice, SecurityHelper securityHelper) {
        this.advertisingRepository = advertisingRepository;
        this.advertisingMapper = advertisingMapper;
        this.userRepository = userRepository;
        this.advertisingPrice = advertisingPrice;
        this.securityHelper = securityHelper;
    }

    public Advertising findById(Long id) {
        return advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException("Объявление с идентификатором id= " + id + " не найдено!"));

    }

    public AdvertisingAllResponseDto findAll() {
        List<AdvertisingOneResponseDto> adsListDto = advertisingRepository.findAll().stream().map(advertisingMapper::toResponse).toList();
        return new AdvertisingAllResponseDto(adsListDto.size(), adsListDto);
    }

    @Transactional
    public AdvertisingOneResponseDto createAds(String username, CreateOrUpdateAd properties, MultipartFile file) {

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь " + username + " не найден!"));

        Advertising advertising = advertisingMapper.toEntity(properties);
        advertising.setAuthor(user);
        advertising.setImage(file.getOriginalFilename());
        //TODO добавить сохранение файла!
        Advertising adsSaved = advertisingRepository.save(advertising);

        return advertisingMapper.toResponse(adsSaved);
    }

    @Transactional
    public AdvertisingOneResponseDto updateById(Long id, CreateOrUpdateAd properties) {
        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException("Объявление с id = " + id + " не найдено!"));

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
                .orElseThrow(() -> new AdvertisingNotFoundException("Объявление с id = " + id + " не найдено!"));
        return advertisingMapper.toResponseWithAuthor(ad);
    }

    @Transactional
    public void deleteById(Long id) {
        advertisingRepository.deleteById(id);
    }

    @Transactional
    public void updateAdsImage(Long id, MultipartFile file) {

        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException("Объявление с id = " + id + " не найдено!"));

        String oldImage = ad.getImage();
        try {
            ad.setImage(file.getOriginalFilename());
            advertisingRepository.save(ad);
            //TODO добавить сохранение файла и удаление старого!
        } catch (Exception e) {
            ad.setImage(oldImage);
            advertisingRepository.save(ad);
        }
    }

    public boolean isAnotherAuthor(Long adsId) {
        Advertising ad = advertisingRepository.findById(adsId)
                .orElseThrow(() -> new AdvertisingNotFoundException("Объявление с id = " + adsId + " не найдено!"));
        return !ad.getAuthor().getId().equals(securityHelper.getCurrentUserId());
    }
}
