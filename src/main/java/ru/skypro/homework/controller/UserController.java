package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.SetPasswordRequestDto;
import ru.skypro.homework.dto.UserInfoResponseDto;
import ru.skypro.homework.dto.UserUpdateInfoDto;
import ru.skypro.homework.security.SecurityHelper;
import ru.skypro.homework.service.UserService;

import java.io.IOException;

/**
 * Контроллер для управления данными пользователя: обновление пароля, получение и частичное обновление профиля,
 * загрузка аватара.
 * <p>
 * Все методы требуют авторизации—проверка выполняется через {@link SecurityHelper}.
 * Контроллер документирован в Swagger, включает валидацию входных данных и логирование ключевых операций.
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final SecurityHelper securityHelper;

    /**
     * Обновляет пароль текущего авторизованного пользователя.
     * <p>
     * Для успешного обновления требуется корректный текущий пароль.
     * </p>
     *
     * @param request DTO с текущим и новым паролем
     * @return {@link ResponseEntity} со статусом 200 при успехе; возможные ошибки обрабатываются на уровне сервиса
     */
    @PostMapping("/set_password")
    @Operation(
            summary = "Изменение пароля",
            description = "Обновляет пароль текущего авторизованного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пароль успешно обновлён"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные пароля",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<String> passwordUpdate(@RequestBody @Valid SetPasswordRequestDto request) {
        String username = securityHelper.getCurrentUsername();
        log.info("Attempting password update for user: {}", username);
        userService.passwordUpdate(
                username,
                request.currentPassword(),
                request.newPassword()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * Получает данные текущего авторизованного пользователя.
     *
     * @return {@link ResponseEntity} с {@link UserInfoResponseDto} при успехе
     */
    @GetMapping("/me")
    @Operation(
            summary = "Информация о пользователе",
            description = "Получение информации об авторизованном пользователе"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о пользователе получена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<UserInfoResponseDto> getUsersInfo() {
        String username = securityHelper.getCurrentUsername();
        log.debug("Fetching user info for: {}", username);
        return ResponseEntity.ok(userService.getUserInfo(username));
    }

    /**
     * Частично обновляет данные профиля текущего авторизованного пользователя (имя, фамилия, телефон и т. п.).
     *
     * @param request DTO с обновляемыми данными пользователя
     * @return {@link ResponseEntity} с обновлёнными данными в формате {@link UserUpdateInfoDto}
     */
    @PatchMapping("/me")
    @Operation(
            summary = "Частичное обновление информации о пользователе",
            description = "Обновление имени, фамилии, телефона авторизованного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные пользователя обновлены"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<UserUpdateInfoDto> updateUsersInfo(@RequestBody @Valid UserUpdateInfoDto request) {
        String username = securityHelper.getCurrentUsername();
        log.info("Updating user info for: {}", username);
        return ResponseEntity.ok(userService.updateUser(username, request));
    }

    /**
     * Загружает или обновляет аватар текущего авторизованного пользователя.
     *
     * @param image файл изображения аватара
     * @return {@link ResponseEntity} со статусом 200 при успешной загрузке
     */
    @PatchMapping("/me/image")
    @Operation(
            summary = "Обновление аватара пользователя",
            description = "Сохранение или обновление аватара авторизованного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар успешно сохранён"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<String> updateUsersAvatar(@RequestParam("image") @Valid MultipartFile image) {
        String username = securityHelper.getCurrentUsername();
        log.info("Updating avatar for user: {}, file: {}", username, image.getOriginalFilename());
        userService.updateUsersAvatar(username, image);
        return ResponseEntity.ok().build();
    }
}
