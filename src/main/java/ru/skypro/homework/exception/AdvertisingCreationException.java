package ru.skypro.homework.exception;

/**
 * Исключение, возникающее при ошибках создания объявления.
 * <p>
 * Используется в сервисе и контроллере при обработке сценариев, когда создание
 * объявления невозможно из‑за некорректных данных, нарушения бизнес‑правил или
 * технических проблем. Является непроверяемым (RuntimeException), поэтому не требует
 * обязательного перехвата в try‑catch на уровне вызывающего кода.
 * </p>
 */
public class AdvertisingCreationException extends RuntimeException {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message описание ошибки
     */
    public AdvertisingCreationException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с указанным сообщением и причиной.
     * <p>
     * Позволяет сохранить стек вызова и детали исходного исключения, что полезно
     * для отладки и логирования.
     * </p>
     *
     * @param message описание ошибки
     * @param cause   исходное исключение, ставшее причиной ошибки
     */
    public AdvertisingCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
