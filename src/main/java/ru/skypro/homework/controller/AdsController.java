package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdvertisingAllResponseDto;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.service.AdvertisingService;
import ru.skypro.homework.util.SecurityHelper;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ads")
public class AdsController {

    private  final AdvertisingService advertisingService;
    private  final SecurityHelper securityHelper;

    /**
     * Мотод получает список всех объявлений и их количество
     * @return DTO с количеством и списком всех объявлений
     */
    @GetMapping("")
    @Operation(
            summary = "Получить все объявления",
            description = "Получение списка всех объявлений"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Все объявления получены")
    })
    public ResponseEntity<?> getAllAds() {

        return ResponseEntity.ok(advertisingService.findAll());
    }

    /**
     * Метод добавляет одно объявление
     * @param file - рисунок объявления
     * @return DTO одного объявления
     */
    @PostMapping("")
    @Operation(
            summary = "Добавить объявление",
            description = "Добавление нового бъявление"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Объявление добавлено"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<?> addAds(@RequestParam("file") @Valid MultipartFile file) {

        if (!securityHelper.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(advertisingService.createAds(securityHelper.getCurrentUsername(), file));
    }

    /**
     * Метод получает объявления по его id
     * @return DTO c данными объявления и его автора
     */
    @GetMapping("/ads/{id}")
    public ResponseEntity<AdvertisingWithAuthorDto> getAdsById(@PathVariable @Valid Long id) {

        if (!securityHelper.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AdvertisingWithAuthorDto ads = advertisingService.getAdById(securityHelper.getCurrentUserId());
        return ResponseEntity.ok(ads);
    }

    /**
     * Метод получает все объявления авторизованного пользователя
     * @return DTO с количеством объявлений и их список
     */
    @Operation(
            summary = "Получить все объявление авторизованного пользователя",
            description = "Получить все объявление авторизованного пользователя и их количство"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Объяления и их количество получено"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping("/ads/me")
    public ResponseEntity<AdvertisingAllResponseDto> getAdsAuthorisedUser() {

        if (!securityHelper.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AdvertisingAllResponseDto ads = advertisingService.findAllByUserId(securityHelper.getCurrentUserId());
        return ResponseEntity.ok(ads);
    }
}
