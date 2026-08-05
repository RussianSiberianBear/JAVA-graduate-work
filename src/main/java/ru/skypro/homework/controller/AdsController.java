package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdvertisingAllResponseDto;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.dto.CommentsAllResponseDto;
import ru.skypro.homework.service.AdvertisingService;
import ru.skypro.homework.service.CommentService;
import ru.skypro.homework.util.SecurityHelper;

import java.io.IOException;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ads")
public class AdsController {

    private final AdvertisingService advertisingService;
    private final CommentService commentService;
    private final SecurityHelper securityHelper;

    /**
     * Мотод получает список всех объявлений и их количество
     *
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
     *
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
     *
     * @return DTO c данными объявления и его автора
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdvertisingWithAuthorDto> getAdsById(@PathVariable @Valid Long id) {

        if (!securityHelper.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AdvertisingWithAuthorDto ads = advertisingService.getAdById(id);
        return ResponseEntity.ok(ads);
    }

    /**
     * Метод удаляет объявления по его id
     *
     * @return Статус 204 при успешном удалении
     * 401 при неавторизованном пользователе
     * 403 при недостатке прав
     * 404 если объявление не найдено
     */
    @Operation(
            summary = "Удалить объявление по его id",
            description = "Удалить объявление по его id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Успешное удаление"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<AdvertisingWithAuthorDto> deleteAdsById(@PathVariable @Valid Long id) {

        if (!securityHelper.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!securityHelper.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        advertisingService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Метод получает все объявления авторизованного пользователя
     *
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
    @GetMapping("/me")
    public ResponseEntity<AdvertisingAllResponseDto> getAdsAuthorisedUser() {

        if (!securityHelper.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AdvertisingAllResponseDto ads = advertisingService.findAllByUserId(securityHelper.getCurrentUserId());
        return ResponseEntity.ok(ads);
    }

    /**
     * Метод сохраняет или обновляет аватар пользователя
     *
     * @param file - аватар пользователя
     * @return Статус 200 при успешном обновлении или
     * статус 401 если пользователь не авторизован
     */
    @PatchMapping("/{id}/image")
    @Operation(
            summary = "Обновление картинки объявления",
            description = "Обновление картинки объявления с идентификатором id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Картинкаобъявления обновлена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостатчно прав"),
            @ApiResponse(responseCode = "404", description = "Картинка с заданным идентификатором не найдена")
    })
    public ResponseEntity<?> updateAdsImage(@PathVariable @Valid Long id, @RequestParam("file") @Valid MultipartFile file) throws IOException {

        if (!securityHelper.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!securityHelper.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") ||
                        auth.getAuthority().equals("ROLE_USER"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        advertisingService.updateAdsImage(id, file);
        return ResponseEntity.ok(file.getOriginalFilename());
    }

    /**
     * Получение всех комментариев по заданному рекламному объявлению
     * @param id - идентификатор рекламного объявления
     * @return DTO всех объявлений и их количества
     */
    @Operation(
            summary = "Получить все комментарии по заданному рекламному объявлению",
            description = "Получить все комментарии по заданному рекламному объявлению и их количство"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарии и их количество получено"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "404", description = "Заданное объявление не найдено")
    })
    @GetMapping("/{id}/comments")
    public ResponseEntity<CommentsAllResponseDto> getAllCommentsByAdsId(@PathVariable @Valid Long id) {

        if (!securityHelper.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(commentService.findByAdvertisingId(id));
    }

}
