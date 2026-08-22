package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.dto.Login;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.service.AuthService;

/**
 * Контроллер для аутентификации и регистрации пользователей.
 * <p>
 * Предоставляет REST‑эндпоинты для входа в систему и создания новых учётных записей.
 * Интегрирован со Swagger для документирования API, использует валидацию входных данных
 * и делегирует основную логику сервису {@link AuthService}.
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Выполняет аутентификацию пользователя по логину и паролю.
     * <p>
     * При успешной проверке учётных данных возвращает статус 200.
     * Если логин или пароль неверны—статус 401.
     * </p>
     *
     * @param login DTO с логином и паролем пользователя
     * @return {@link ResponseEntity} со статусом 200 при успехе или 401 при ошибке аутентификации
     */
    @PostMapping("/login")
    @Operation(
            summary = "Аутентификация пользователя",
            description = "Вход в систему по логину и паролю"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса"),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    })
    public ResponseEntity<String> login(@Valid @RequestBody Login login) {
        log.debug("Attempting login for user: {}", login.username());
        if (authService.login(login.username(), login.password())) {
            log.info("Login successful for user: {}", login.username());
            return ResponseEntity.ok().build();
        } else {
            log.warn("Login failed for user: {}", login.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Регистрирует нового пользователя в системе.
     * <p>
     * При успешном создании учётной записи возвращает статус 201.
     * Если данные некорректны или пользователь с таким логином уже существует — статус 400.
     * </p>
     *
     * @param register DTO с данными для регистрации нового пользователя
     * @return {@link ResponseEntity} со статусом 201 при успехе или 400 при ошибке регистрации
     */
    @PostMapping("/register")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создание нового пользователя в системе"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса или пользователь уже существует")
    })
    public ResponseEntity<String> register(@Valid @RequestBody Register register) {
        log.debug("Attempting registration for user: {}", register.username());
        if (authService.register(register)) {
            log.info("User registered successfully: {}", register.username());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            log.warn("Registration failed for user: {}", register.username());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
