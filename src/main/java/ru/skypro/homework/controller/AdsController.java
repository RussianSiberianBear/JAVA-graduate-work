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

@Slf4j
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
    public ResponseEntity<AdvertisingAllResponseDto> getAllAds() {

        return ResponseEntity.ok(advertisingService.findAll());
    }

    /**
     * Метод добавляет одно объявление
     *
     * @param image - рисунок объявления
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
    public ResponseEntity<AdvertisingOneResponseDto> addAds(@RequestPart("properties") CreateOrUpdateAd properties, @RequestPart("image") MultipartFile image) {

        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(advertisingService.createAds(securityHelper.getCurrentUsername(), properties, image));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Метод получает объявления по его id
     *
     * @return DTO c данными объявления и его автора
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdvertisingWithAuthorDto> getAdsById(@PathVariable @Valid Long id) {


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
    public ResponseEntity<String> deleteAdsById(@PathVariable @Valid Long id) {


        if (!securityHelper.isAdmin() && advertisingService.isAnotherAuthor(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        advertisingService.deleteAdById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Метод обновляет объявления по его id
     *
     * @return Статус 204 при успешном удалении
     * 401 при неавторизованном пользователе
     * 403 при недостатке прав
     * 404 если объявление не найдено
     */
    @Operation(
            summary = "Обновить объявление по его id",
            description = "Обновитьить объявление по его id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Успешное обновление"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<AdvertisingOneResponseDto> updateAdsById(@RequestBody CreateOrUpdateAd properties, @PathVariable @Valid Long id) {


        if (!securityHelper.isAdmin() && advertisingService.isAnotherAuthor(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(advertisingService.updateById(id, properties));
    }

    /**
     * Метод получает все объявления авторизованного пользователя
     *
     * @return DTO с количеством объявлений и их список
     */
    @Operation(
            summary = "Получить все объявление авторизованного пользователя",
            description = "Получить все объявление авторизованного пользователя и их количество"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Объяления и их количество получено"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping("/me")
    public ResponseEntity<AdvertisingAllResponseDto> getAdsAuthorisedUser() {


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
    public ResponseEntity updateAdsImage(@PathVariable @Valid Long id, @RequestParam("image") @Valid MultipartFile image) {

        if (!securityHelper.isAdmin() && advertisingService.isAnotherAuthor(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        advertisingService.updateAdsImage(id, image);
        return ResponseEntity.ok(image.getOriginalFilename());
    }

    /**
     * Получение всех комментариев по заданному рекламному объявлению
     *
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


        return ResponseEntity.ok(commentService.findByAdvertisingId(id));
    }

    /**
     * Добавление комментария к заданному рекламному объявлению
     *
     * @param id   - идентификатор объявления
     * @param text - текст комментария
     * @return DTO добавленного комментария
     */

    @Operation(
            summary = "Добавить комментарий к заданному рекламному объявлению",
            description = "Добавить комментарий к заданному рекламному объявлению по его id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарии и их количество получено"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "404", description = "Заданное объявление не найдено")
    })
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentOneResponseDto> addCommentToAdvertisingId(@PathVariable @Valid Long id, @Valid @RequestBody CommentRequestDto text) {


        return ResponseEntity.ok(commentService.addCommentToAdvertisingId(securityHelper.getCurrentUser(), id, text));
    }

    /**
     * Метод удалает определенный комментарий к конкретному рекламному объявлению
     *
     * @param adId      - идентификатор рекламного объявления
     * @param commentId - идентификатор комментария
     * @return Статус 200 - при удачном удалении
     * 401 - при попытке ваыполнить операцию неавторизованным пользователем
     * 403 - при недосточном уровне прав пользователя(удаление комментариев разрешено только пользователю с рольюю ADMIN)
     * 404 - если комментари  или само обяъвление не найдено
     */
    @Operation(
            summary = "Удалить заданный комментарий к заданному рекламному объявлению",
            description = "Удалить заданный комментарий к заданному рекламному объявлению"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий удален"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостатчно прав"),
            @ApiResponse(responseCode = "404", description = "Заданное объявление или комментарий не найдены")
    })
    @DeleteMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable @Valid Long adId, @PathVariable @Valid Long commentId) {

        if (!securityHelper.isAdmin() && commentService.isAnotherAuthor(commentId, adId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        commentService.deleteCommentByIdAndAdvertisingById(commentId, adId);

        return ResponseEntity.ok().build();
    }

    /**
     * Метод обновляет определенный комментарий к конкретному рекламному объявлению
     *
     * @param adId      - идентификатор рекламного объявления
     * @param commentId - идентификатор комментария
     * @return Статус 200 и DTO обновленного комментария при удачном обновлении
     * 401 - при попытке ваыполнить операцию неавторизованным пользователем
     * 403 - при недосточном уровне прав пользователя(удаление комментариев разрешено только пользователю с рольюю ADMIN)
     * 404 - если комментари  или само обяъвление не найдено
     */
    @Operation(
            summary = "Обновить заданный комментарий к заданному рекламному объявлению",
            description = "Обновить заданный комментарий к заданному рекламному объявлению"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий удален"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостатчно прав"),
            @ApiResponse(responseCode = "404", description = "Заданное объявление или комментарий не найдены")
    })
    @PatchMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<CommentOneResponseDto> updateComment(@PathVariable @Valid Long adId, @PathVariable @Valid Long commentId, @Valid @RequestBody CommentRequestDto text) {

        if (!securityHelper.isAdmin() && commentService.isAnotherAuthor(commentId, adId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(commentService.updateCommentByIdAndAdvertisingById(commentId, adId, text));
    }
}
