// Исключение для ошибок при получении объявления
package ru.skypro.homework.exception;

public class AdvertisingRetrievalException extends RuntimeException {
    public AdvertisingRetrievalException(String message) {
        super(message);
    }

    public AdvertisingRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}