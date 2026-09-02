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
import ru.skypro.homework.dto.*;
import ru.skypro.homework.security.SecurityHelper;
import ru.skypro.homework.service.AdvertisingService;
import ru.skypro.homework.service.CommentService;

import java.io.IOException;

/**
 * Контроллер для работы с объявлениями и комментариями.
 * <p>
 * Предоставляет REST‑эндпоинты для CRUD‑операций над объявлениями, а также для управления
 * комментариями к ним. Контроллер интегрирован со Swagger для документирования API,
 * использует валидацию входных данных и проверку прав доступа через {@link SecurityHelper}.
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ads")
public class AdsController {

    private final AdvertisingService advertisingService;
    private final CommentService commentService;
    private final SecurityHelper securityHelper;

    /**
     * Получает список всех объявлений и их общее количество.
     * <p>
     * Доступен без авторизации (по контракту с фронтендом).
     * </p>
     *
     * @return {@link ResponseEntity} с {@link AdvertisingAllResponseDto}, содержащим количество и список объявлений
     */
    @GetMapping("")
    @Operation(
            summary = "Получить все объявления",
            description = "Получение списка всех объявлений"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Все объявления получены")
    })
    public ResponseEntity<AdvertisingAllResponseDto> getAllAds() {
        log.debug("Fetching all ads");
        return ResponseEntity.ok(advertisingService.findAll());
    }

    /**
     * Добавляет новое объявление с изображением.
     * <p>
     * Требует авторизации. Принимает DTO с данными объявления и файл изображения.
     * </p>
     *
     * @param properties данные для создания объявления ({@link CreateOrUpdateAd})
     * @param image      файл изображения объявления
     * @return {@link ResponseEntity} со статусом 201 и DTO созданного объявления
     */
    @PostMapping("")
    @Operation(
            summary = "Добавить объявление",
            description = "Добавление нового объявления с изображением"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Объявление добавлено"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "500", description = "Ошибка при обработке файла")
    })
    public ResponseEntity<AdvertisingOneResponseDto> addAds(
            @RequestPart("properties") @Valid CreateOrUpdateAd properties,
            @RequestPart("image") MultipartFile image) {

        try {
            log.debug("Creating new ad for user: {}", securityHelper.getCurrentUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(advertisingService.createAds(securityHelper.getCurrentUsername(), properties, image));
        } catch (IOException e) {
            log.error("Failed to process image file during ad creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получает объявление по его идентификатору вместе с данными автора.
     *
     * @param id идентификатор объявления
     * @return {@link ResponseEntity} с {@link AdvertisingWithAuthorDto}
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Получить объявление по ID",
            description = "Получение объявления и информации об его авторе по идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Объявление найдено"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    public ResponseEntity<AdvertisingWithAuthorDto> getAdsById(@PathVariable @Valid Long id) {
        log.debug("Fetching ad with id: {}", id);
        AdvertisingWithAuthorDto ads = advertisingService.getAdById(id);
        return ResponseEntity.ok(ads);
    }

    /**
     * Удаляет объявление по его идентификатору.
     * <p>
     * Доступ разрешён только автору объявления или администратору.
     * </p>
     *
     * @param id идентификатор объявления
     * @return {@link ResponseEntity} со статусом 204 при успешном удалении, 403 при отсутствии прав, 404 если не найдено
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить объявление по ID",
            description = "Удаление объявления по идентификатору (доступно автору или администратору)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Объявление успешно удалено"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    public ResponseEntity<String> deleteAdsById(@PathVariable @Valid Long id) {
        if (!securityHelper.isAdmin() && advertisingService.isAnotherAuthor(id)) {
            log.warn("Access denied for user {} to delete ad id {}", securityHelper.getCurrentUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Deleting ad with id: {}", id);
        advertisingService.deleteAdById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Обновляет данные объявления по его идентификатору.
     * <p>
     * Доступ разрешён только автору объявления или администратору.
     * </p>
     *
     * @param properties новые данные для обновления объявления
     * @param id         идентификатор объявления
     * @return {@link ResponseEntity} с обновлённым объявлением в формате DTO
     */
    @PatchMapping("/{id}")
    @Transactional
    @Operation(
            summary = "Обновить объявление по ID",
            description = "Обновление данных объявления (доступно автору или администратору)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Объявление успешно обновлено"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    public ResponseEntity<AdvertisingOneResponseDto> updateAdsById(
            @RequestBody @Valid CreateOrUpdateAd properties,
            @PathVariable @Valid Long id) {

        if (!securityHelper.isAdmin() && advertisingService.isAnotherAuthor(id)) {
            log.warn("Access denied for user {} to update ad id {}", securityHelper.getCurrentUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Updating ad with id: {}", id);
        return ResponseEntity.ok(advertisingService.updateById(id, properties));
    }

    /**
     * Получает все объявления текущего авторизованного пользователя.
     *
     * @return {@link ResponseEntity} с количеством и списком объявлений пользователя
     */
    @GetMapping("/me")
    @Operation(
            summary = "Получить объявления авторизованного пользователя",
            description = "Получение всех объявлений, созданных текущим пользователем"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список объявлений получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<AdvertisingAllResponseDto> getAdsAuthorisedUser() {
        Long userId = securityHelper.getCurrentUserId();
        log.debug("Fetching ads for user id: {}", userId);
        AdvertisingAllResponseDto ads = advertisingService.findAllByUserId(userId);
        return ResponseEntity.ok(ads);
    }

    /**
     * Обновляет изображение объявления.
     * <p>
     * Доступ разрешён только автору объявления или администратору.
     * </p>
     *
     * @param id    идентификатор объявления
     * @param image файл нового изображения
     * @return {@link ResponseEntity} со статусом 200 и именем файла при успешном обновлении
     */
    @PatchMapping("/{id}/image")
    @Operation(
            summary = "Обновить изображение объявления",
            description = "Замена изображения объявления (доступно автору или администратору)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Изображение обновлено"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    public ResponseEntity<?> updateAdsImage(
            @PathVariable @Valid Long id,
            @RequestParam("image") @Valid MultipartFile image) {

        if (!securityHelper.isAdmin() && advertisingService.isAnotherAuthor(id)) {
            log.warn("Access denied for user {} to update image of ad id {}", securityHelper.getCurrentUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Updating image for ad id: {}", id);
        advertisingService.updateAdsImage(id, image);
        return ResponseEntity.ok(image.getOriginalFilename());
    }

    /**
     * Получает все комментарии к объявлению по его идентификатору.
     *
     * @param id идентификатор объявления
     * @return {@link ResponseEntity} с DTO, содержащим комментарии и их количество
     */
    @GetMapping("/{id}/comments")
    @Operation(
            summary = "Получить комментарии к объявлению",
            description = "Получение всех комментариев, привязанных к объявлению"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарии получены"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    public ResponseEntity<CommentsAllResponseDto> getAllCommentsByAdsId(@PathVariable @Valid Long id) {
        log.debug("Fetching comments for ad id: {}", id);
        return ResponseEntity.ok(commentService.findByAdvertisingId(id));
    }

    /**
     * Добавляет комментарий к объявлению.
     *
     * @param id   идентификатор объявления
     * @param text DTO с текстом комментария
     * @return {@link ResponseEntity} с DTO добавленного комментария
     */
    @PostMapping("/{id}/comments")
    @Operation(
            summary = "Добавить комментарий к объявлению",
            description = "Создание нового комментария к объявлению от имени текущего пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий добавлен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    public ResponseEntity<CommentOneResponseDto> addCommentToAdvertisingId(
            @PathVariable @Valid Long id,
            @Valid @RequestBody CommentRequestDto text) {

        log.info("Adding comment to ad id: {}", id);
        return ResponseEntity.ok(
                commentService.addCommentToAdvertisingId(securityHelper.getCurrentUser(), id, text)
        );
    }

    /**
     * Удаляет комментарий, привязанный к объявлению.
     * <p>
     * Удаление разрешено только администратору или автору комментария.
     * </p>
     *
     * @param adId      идентификатор объявления
     * @param commentId идентификатор комментария
     * @return {@link ResponseEntity} со статусом 200 при успешном удалении
     */
    @DeleteMapping("/{adId}/comments/{commentId}")
    @Operation(
            summary = "Удалить комментарий к объявлению",
            description = "Удаление комментария (доступно администратору или автору комментария)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий удалён"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Объявление или комментарий не найдены")
    })
    public ResponseEntity<String> deleteComment(
            @PathVariable @Valid Long adId,
            @PathVariable @Valid Long commentId) {

        if (!securityHelper.isAdmin() && commentService.isAnotherAuthor(commentId, adId)) {
            log.warn("Access denied for user {} to delete comment id {}", securityHelper.getCurrentUsername(), commentId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Deleting comment id: {} for ad id: {}", commentId, adId);
        commentService.deleteCommentByIdAndAdvertisingById(commentId, adId);
        return ResponseEntity.ok().build();
    }

    /**
     * Обновляет текст комментария, привязанного к объявлению.
     * <p>
     * Обновление разрешено только администратору или автору комментария.
     * </p>
     *
     * @param adId      идентификатор объявления
     * @param commentId идентификатор комментария
     * @param text      DTO с новым текстом комментария
     * @return {@link ResponseEntity} с DTO обновлённого комментария
     */
    @PatchMapping("/{adId}/comments/{commentId}")
    @Operation(
            summary = "Обновить комментарий к объявлению",
            description = "Изменение текста комментария (доступно администратору или автору комментария)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий обновлён"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Объявление или комментарий не найдены")
    })
    public ResponseEntity<CommentOneResponseDto> updateComment(
            @PathVariable @Valid Long adId,
            @PathVariable @Valid Long commentId,
            @Valid @RequestBody CommentRequestDto text) {

        if (!securityHelper.isAdmin() && commentService.isAnotherAuthor(commentId, adId)) {
            log.warn("Access denied for user {} to update comment id {}", securityHelper.getCurrentUsername(), commentId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Updating comment id: {} for ad id: {}", commentId, adId);
        return ResponseEntity.ok(
                commentService.updateCommentByIdAndAdvertisingById(commentId, adId, text)
        );
    }
}
