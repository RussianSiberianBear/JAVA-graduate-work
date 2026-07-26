package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.skypro.homework.service.UserService;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class UserController {

    @Value("${security.min-password-length:6}")
    private int minPasswordLength;

    private final UserService userService;

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
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<?> password_update(@RequestBody String password) {

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым!");
        }

        if (password.length() < minPasswordLength) {
            throw new IllegalArgumentException(
                    "Пароль должен содержать минимум " + minPasswordLength + " символов!"
            );
        }

        if (authService.login(login.username(), login.password())) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
