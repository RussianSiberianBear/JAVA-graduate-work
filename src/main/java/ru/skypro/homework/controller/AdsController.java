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
import ru.skypro.homework.model.Advertising;
import ru.skypro.homework.service.AdvertisingService;

import java.util.List;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ads")
public class AdsController {

    private  final AdvertisingService advertisingService;

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
     * @param authentication - объект аутентификации пользователя
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
    public ResponseEntity<?> addAds(@RequestParam("file") @Valid MultipartFile file, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(advertisingService.createAds(username, file));
    }

}
