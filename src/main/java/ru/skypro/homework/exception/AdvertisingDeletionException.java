// Исключение для ошибок при удалении объявления
package ru.skypro.homework.exception;

public class AdvertisingDeletionException extends RuntimeException {
    public AdvertisingDeletionException(String message) {
        super(message);
    }

    public AdvertisingDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}