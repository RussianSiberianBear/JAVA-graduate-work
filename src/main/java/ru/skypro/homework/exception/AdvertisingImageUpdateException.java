// Исключение для ошибок при обновлении изображения
package ru.skypro.homework.exception;

public class AdvertisingImageUpdateException extends RuntimeException {
    public AdvertisingImageUpdateException(String message) {
        super(message);
    }

    public AdvertisingImageUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}