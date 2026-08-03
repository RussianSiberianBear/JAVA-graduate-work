package ru.skypro.homework.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdvertisingAllResponseDto;
import ru.skypro.homework.dto.AdvertisingOneResponseDto;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.exception.AdvertisingNotFoundException;
import ru.skypro.homework.mapper.AdvertisingMapper;
import ru.skypro.homework.model.Advertising;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.AdvertisingRepository;
import ru.skypro.homework.repository.UserRepository;

import java.util.List;

@Service
public class AdvertisingService {

    private final AdvertisingRepository advertisingRepository;
    private final AdvertisingMapper advertisingMapper;
    private final UserRepository userRepository;
    private final AdvertisingPrice advertisingPrice;

    public AdvertisingService(AdvertisingRepository advertisingRepository, AdvertisingMapper advertisingMapper, UserRepository userRepository, AdvertisingPrice advertisingPrice) {
        this.advertisingRepository = advertisingRepository;
        this.advertisingMapper = advertisingMapper;
        this.userRepository = userRepository;
        this.advertisingPrice = advertisingPrice;
    }

    public Advertising findById(Long id) {
        return advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException("Объявление с идентификатором id= " + id + " не найдено!"));

    }

    public AdvertisingAllResponseDto findAll() {
        List<AdvertisingOneResponseDto> adsListDto = advertisingRepository.findAll().stream().map(advertisingMapper::toResponse).toList();
        return new AdvertisingAllResponseDto(adsListDto.size(), adsListDto);
    }

    public AdvertisingOneResponseDto createAds(String username, MultipartFile file) {

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь " + username + " не найден!"));

        Advertising advertising = new Advertising();
        advertising.setAuthor(user);
        advertising.setImage(file.getOriginalFilename());
        //TODO добавить сохранение файла!
        advertising.setTitle(file.getContentType());
        advertising.setPrice(advertisingPrice.getPrice());
        Advertising adsSaved = advertisingRepository.save(advertising);

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

    public void deleteById(Long id) {
        advertisingRepository.deleteById(id);
    }
}
