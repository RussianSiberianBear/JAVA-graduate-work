package ru.skypro.homework.exception;

/**
 * Исключение, возникающее при ошибках удаления объявления.
 * <p>
 * Используется в сервисе и контроллере при обработке сценариев, когда удаление
 * объявления невозможно — например, из‑за отсутствия записи, нарушения бизнес‑правил
 * (например, нельзя удалить объявление с активными заказами) или технических проблем.
 * Является непроверяемым (RuntimeException), поэтому не требует обязательного
 * перехвата в try‑catch на уровне вызывающего кода.
 * </p>
 */
public class AdvertisingDeletionException extends RuntimeException {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message описание ошибки
     */
    public AdvertisingDeletionException(String message) {
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
    public AdvertisingDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
