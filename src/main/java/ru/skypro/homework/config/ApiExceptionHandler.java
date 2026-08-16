package ru.skypro.homework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.skypro.homework.exception.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(annotations = org.springframework.web.bind.annotation.RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("Unexpected API error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("API illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // ============ Ошибки "Не найдено" (404) ============

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.warn("API username not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AdvertisingNotFoundException.class)
    public ResponseEntity<String> handleAdvertisingNotFoundException(AdvertisingNotFoundException ex) {
        log.warn("API advertising not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<String> handleCommentNotFoundException(CommentNotFoundException ex) {
        log.warn("API comment not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // ============ Ошибки валидации (400) ============

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("API invalid password: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

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

    // ============ Ошибки бизнес-логики (400 / 409) ============

    @ExceptionHandler(AdvertisingCreationException.class)
    public ResponseEntity<String> handleAdvertisingCreationException(AdvertisingCreationException ex) {
        log.warn("API advertising creation error: {}", ex.getMessage());
        // Если ошибка связана с файловым хранилищем или БД - 500, иначе 400
        if (ex.getCause() instanceof FileStorageException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File storage error: " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(AdvertisingUpdateException.class)
    public ResponseEntity<String> handleAdvertisingUpdateException(AdvertisingUpdateException ex) {
        log.warn("API advertising update error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(AdvertisingDeletionException.class)
    public ResponseEntity<String> handleAdvertisingDeletionException(AdvertisingDeletionException ex) {
        log.warn("API advertising deletion error: {}", ex.getMessage());
        if (ex.getCause() instanceof FileStorageException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File storage error: " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete ad: " + ex.getMessage());
    }

    @ExceptionHandler(AdvertisingImageUpdateException.class)
    public ResponseEntity<String> handleAdvertisingImageUpdateException(AdvertisingImageUpdateException ex) {
        log.warn("API advertising image update error: {}", ex.getMessage());
        if (ex.getCause() instanceof FileStorageException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File storage error: " + ex.getMessage());
        }
        if (ex.getCause() instanceof java.io.IOException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file: " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update image: " + ex.getMessage());
    }

    @ExceptionHandler(AdvertisingRetrievalException.class)
    public ResponseEntity<String> handleAdvertisingRetrievalException(AdvertisingRetrievalException ex) {
        log.warn("API advertising retrieval error: {}", ex.getMessage());
        // Если причина - не найдено, то 404, иначе 500
        if (ex.getCause() instanceof AdvertisingNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve ad: " + ex.getMessage());
    }

    // ============ Ошибки файлового хранилища (500 / 503) ============

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<String> handleFileStorageException(FileStorageException ex) {
        log.error("API file storage error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("File storage service error: " + ex.getMessage());
    }

    // ============ Ошибки доступа (403) ============

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        log.warn("API access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + ex.getMessage());
    }
}