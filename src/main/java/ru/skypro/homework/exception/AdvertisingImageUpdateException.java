package ru.skypro.homework.exception;

/**
 * Исключение, возникающее при ошибках обновления изображения объявления.
 * <p>
 * Используется в сервисе и контроллере при обработке сценариев, когда обновление
 * изображения невозможно — например, из‑за некорректного файла, проблем с загрузкой,
 * ошибок сохранения в хранилище или нарушения бизнес‑правил (например, недопустимый
 * формат или размер файла). Является непроверяемым (RuntimeException), поэтому
 * не требует обязательного перехвата в try‑catch на уровне вызывающего кода.
 * </p>
 */
public class AdvertisingImageUpdateException extends RuntimeException {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message описание ошибки
     */
    public AdvertisingImageUpdateException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с указанным сообщением и причиной.
     * <p>
     * Позволяет сохранить стек вызова и детали исходного исключения — это полезно
     * для отладки и логирования.
     * </p>
     *
     * @param message описание ошибки
     * @param cause   исходное исключение, ставшее причиной ошибки
     */
    public AdvertisingImageUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
