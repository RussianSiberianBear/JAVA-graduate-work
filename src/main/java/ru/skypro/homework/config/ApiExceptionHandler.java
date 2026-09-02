package ru.skypro.homework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.skypro.homework.exception.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для REST-контроллеров приложения.
 * <p>
 * Класс перехватывает различные типы исключений, возникающих в API, и возвращает
 * структурированные HTTP-ответы с соответствующими кодами состояния.
 * Обработчик ориентирован на предоставление понятных сообщений клиенту,
 * при этом детали ошибок логируются на уровне ERROR/WARN.
 * </p>
 */
@Slf4j
@RestControllerAdvice(annotations = org.springframework.web.bind.annotation.RestController.class)
public class ApiExceptionHandler {

    /**
     * Обрабатывает исключение {@link UsernameNotFoundException}.
     * Возвращает ответ со статусом 404 и сообщением о том, что пользователь не найден.
     *
     * @param e исключение, возникшее при поиске пользователя
     * @return {@link ResponseEntity} со статусом 404 и текстовым сообщением об ошибке
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleUsernameNotFound(UsernameNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("User not found. " + e.getMessage());
    }

    /**
     * Обрабатывает все необработанные исключения типа {@link Exception}.
     * Логирует ошибку на уровне ERROR и возвращает ответ со статусом 500.
     *
     * @param ex общее исключение, не обработанное другими методами
     * @return {@link ResponseEntity} со статусом 500 и сообщением об ошибке сервера
     */
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("Unexpected API error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error: " + ex.getMessage());
    }

    /**
     * Обрабатывает исключения {@link IllegalArgumentException}.
     * Логирует предупреждение и возвращает ответ со статусом 400.
     *
     * @param ex исключение, связанное с некорректными аргументами
     * @return {@link ResponseEntity} со статусом 400 и сообщением об ошибке
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("API illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    // ============ Ошибки "Не найдено" (404) ============

    /**
     * Обрабатывает исключение {@link AdvertisingNotFoundException}.
     * Логирует предупреждение и возвращает ответ со статусом 404.
     *
     * @param ex исключение, указывающее на отсутствие объявления
     * @return {@link ResponseEntity} со статусом 404 и сообщением об ошибке
     */
    @ExceptionHandler(AdvertisingNotFoundException.class)
    public ResponseEntity<String> handleAdvertisingNotFoundException(AdvertisingNotFoundException ex) {
        log.warn("API advertising not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    /**
     * Обрабатывает исключение {@link CommentNotFoundException}.
     * Логирует предупреждение и возвращает ответ со статусом 404.
     *
     * @param ex исключение, указывающее на отсутствие комментария
     * @return {@link ResponseEntity} со статусом 404 и сообщением об ошибке
     */
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<String> handleCommentNotFoundException(CommentNotFoundException ex) {
        log.warn("API comment not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    // ============ Ошибки валидации (400) ============

    /**
     * Обрабатывает исключение {@link InvalidPasswordException}.
     * Логирует предупреждение и возвращает JSON-объект с полем "error".
     *
     * @param ex исключение, связанное с некорректным паролем
     * @return {@link ResponseEntity} со статусом 400 и JSON-ответом вида {"error": "..."}
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("API invalid password: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Обрабатывает исключения валидации аргументов контроллера
     * ({@link MethodArgumentNotValidException}).
     * Формирует карту ошибок, где ключ — имя поля, значение — сообщение об ошибке.
     *
     * @param ex исключение валидации, содержащее список ошибок полей
     * @return {@link ResponseEntity} со статусом 400 и JSON-картой ошибок валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        log.warn("API validation errors: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    // ============ Ошибки бизнес-логики (400 / 409 / 500) ============

    /**
     * Обрабатывает исключение при создании объявления ({@link AdvertisingCreationException}).
     * Если причина ошибки — проблема с файловым хранилищем, возвращается статус 500,
     * в остальных случаях — 400.
     *
     * @param ex исключение, возникшее при попытке создания объявления
     * @return {@link ResponseEntity} с соответствующим статусом и сообщением об ошибке
     */
    @ExceptionHandler(AdvertisingCreationException.class)
    public ResponseEntity<String> handleAdvertisingCreationException(AdvertisingCreationException ex) {
        log.warn("API advertising creation error: {}", ex.getMessage());
        if (ex.getCause() instanceof FileStorageException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File storage error: " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    /**
     * Обрабатывает исключение при обновлении объявления ({@link AdvertisingUpdateException}).
     * Возвращает ответ со статусом 400.
     *
     * @param ex исключение, возникшее при попытке обновления объявления
     * @return {@link ResponseEntity} со статусом 400 и сообщением об ошибке
     */
    @ExceptionHandler(AdvertisingUpdateException.class)
    public ResponseEntity<String> handleAdvertisingUpdateException(AdvertisingUpdateException ex) {
        log.warn("API advertising update error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    /**
     * Обрабатывает исключение при удалении объявления ({@link AdvertisingDeletionException}).
     * Если причина — проблема с файловым хранилищем, возвращается статус 500.
     * В остальных случаях также возвращается 500 (по текущей логике).
     *
     * @param ex исключение, возникшее при попытке удаления объявления
     * @return {@link ResponseEntity} с соответствующим статусом и сообщением об ошибке
     */
    @ExceptionHandler(AdvertisingDeletionException.class)
    public ResponseEntity<String> handleAdvertisingDeletionException(AdvertisingDeletionException ex) {
        log.warn("API advertising deletion error: {}", ex.getMessage());
        if (ex.getCause() instanceof FileStorageException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File storage error: " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete ad: " + ex.getMessage());
    }

    /**
     * Обрабатывает исключение при обновлении изображения объявления
     * ({@link AdvertisingImageUpdateException}).
     * Дифференцирует ошибки файлового хранилища (500), ошибки ввода-вывода (400)
     * и прочие ошибки (500).
     *
     * @param ex исключение, возникшее при обновлении изображения
     * @return {@link ResponseEntity} с соответствующим статусом и сообщением об ошибке
     */
    @ExceptionHandler(AdvertisingImageUpdateException.class)
    public ResponseEntity<String> handleAdvertisingImageUpdateException(AdvertisingImageUpdateException ex) {
        log.warn("API advertising image update error: {}", ex.getMessage());
        if (ex.getCause() instanceof FileStorageException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File storage error: " + ex.getMessage());
        }
        if (ex.getCause() instanceof java.io.IOException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file: " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to update image: " + ex.getMessage());
    }

    /**
     * Обрабатывает исключение при получении объявления ({@link AdvertisingRetrievalException}).
     * Если причина — отсутствие объявления, возвращается статус 404,
     * иначе — 500.
     *
     * @param ex исключение, возникшее при попытке получения объявления
     * @return {@link ResponseEntity} с соответствующим статусом и сообщением об ошибке
     */
    @ExceptionHandler(AdvertisingRetrievalException.class)
    public ResponseEntity<String> handleAdvertisingRetrievalException(AdvertisingRetrievalException ex) {
        log.warn("API advertising retrieval error: {}", ex.getMessage());
        if (ex.getCause() instanceof AdvertisingNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve ad: " + ex.getMessage());
    }

    // ============ Ошибки файлового хранилища (400 / 500) ============

    /**
     * Обрабатывает исключения, связанные с файловым хранилищем
     * ({@link FileStorageException}).
     * <p>
     * Для ошибок валидации (пустой файл, неверный тип/расширение) возвращает 400.
     * Для остальных ошибок хранилища — 500.
     * </p>
     *
     * @param ex исключение файлового хранилища
     * @return {@link ResponseEntity} со статусом 400 или 500 и сообщением об ошибке
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<String> handleFileStorageException(FileStorageException ex) {
        String message = ex.getMessage();
        boolean isValidationError = message != null &&
                (message.contains("пустым") || message.contains("Допустимы только изображения") || message.contains("Допустимы только файлы"));

        if (isValidationError) {
            log.warn("API file validation error: {}", message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }

        log.error("API file storage error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("File storage service error: " + ex.getMessage());
    }

    // ============ Ошибки доступа (403) ============

    /**
     * Обрабатывает исключения отказа в доступе
     * ({@link AccessDeniedException}).
     * Логирует предупреждение и возвращает ответ со статусом 403.
     *
     * @param ex исключение отказа в доступе к ресурсу
     * @return {@link ResponseEntity} со статусом 403 и сообщением «Access denied»
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("API access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied: " + ex.getMessage());
    }
}
