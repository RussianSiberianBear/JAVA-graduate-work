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

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Метод аутентификации пользователя
     *
     * @param login DTO пользователя (логин и пароль)
     * @return 200 статус при успешной аутентификации
     * 401 статус при неуспешной аутентификации
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

        if (authService.login(login.username(), login.password())) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Регистрация нового пользователя
     *
     * @param register DTO с данными пользователя
     * @return Статус 201 при успешной регистрации
     * 400 при неуспешной регистрации
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

        if (authService.register(register)) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
