// Исключение для ошибок при обновлении объявления
package ru.skypro.homework.exception;

public class AdvertisingUpdateException extends RuntimeException {
    public AdvertisingUpdateException(String message) {
        super(message);
    }

    public AdvertisingUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}