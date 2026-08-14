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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final SecurityHelper securityHelper;

    /**
     * Обновление паролья пользователя
     *
     * @param request DTO для обновления паролья
     * @return 200 статус при успешном обновлении пароля,
     * 400 статус при некорректных данных пароля
     * 401 статус если пользователь не авторизован
     * 403 статус если у пользователя не хватает прав на обновление пароля
     * 404 статус если пользователь не найден в базе данных
     */
    @PostMapping("/set_password")
    @Operation(
            summary = "Изменение пароля",
            description = "Обновляет пароль текущего авторизованного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пароль успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректный пароль",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })

    public ResponseEntity<String> password_update(@RequestBody @Valid SetPasswordRequestDto request) {

        if (!securityHelper.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (securityHelper.getAuthorities().stream()
                .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") ||
                        auth.getAuthority().equals("ROLE_USER"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String username = securityHelper.getCurrentUsername();
        userService.passwordUpdate(
                username,
                request.currentPassword(),
                request.newPassword()
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Получение данных об авторизованном пользователе
     *
     * @return Статус 401 при неавторизованном пользователе или
     * 200 статус и данные авторизованного пользователя
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

        if (!securityHelper.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getUserInfo(securityHelper.getCurrentUsername()));
    }

    /**
     * Обновление информации авторизованного пользователя
     *
     * @param request - DTO обновляеммых данных пользователя
     * @return Статус 401 при неавторизованном пользователе или
     * 200 статус и обновленные данные авторизованного пользователя
     */
    @PatchMapping("/me")
    @Operation(
            summary = "Частичное обновление информация о пользователе",
            description = "Обновление имени, фамилии, телефона авторизованного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Обновленные данные пользователя"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<UserUpdateInfoDto> updateUsersInfo(@RequestBody @Valid UserUpdateInfoDto request) {

        if (!securityHelper.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.updateUser(securityHelper.getCurrentUsername(), request));
    }

    /**
     * Метод сохраняет или обновляет аватар пользователя
     *
     * @param image - аватар пользователя
     * @return Статус 200 при успешном обновлении или
     * статус 401 если пользователь не авторизован
     */
    @PatchMapping("/me/image")
    @Operation(
            summary = "Аватар пользователе",
            description = "Сохранение или обновление аватара авторизованного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар пользователе сохранен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<String> updateUsersAvatar(@RequestParam("image") @Valid MultipartFile image) {

        if (!securityHelper.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            userService.updateUsersAvatar(securityHelper.getCurrentUsername(), image);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
