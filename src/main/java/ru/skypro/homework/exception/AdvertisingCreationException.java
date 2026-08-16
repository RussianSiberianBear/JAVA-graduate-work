// Исключение для ошибок при создании объявления
package ru.skypro.homework.exception;

public class AdvertisingCreationException extends RuntimeException {
    public AdvertisingCreationException(String message) {
        super(message);
    }

    public AdvertisingCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}