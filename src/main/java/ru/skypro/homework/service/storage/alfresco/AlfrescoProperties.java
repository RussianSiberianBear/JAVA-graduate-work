package ru.skypro.homework.service.storage.alfresco;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурационные свойства для подключения к Alfresco.
 * <p>
 * Значения задаются в application.yml/properties с префиксом {@code alfresco}:
 * <pre>{@code
 * alfresco:
 *   url: http://localhost:8080
 *   username: admin
 *   password: admin
 *   folderId: abcd-efgh-ijkl-mnop
 * }</pre>
 * </p>
 * <p>
 * Учитывая твой контекст (работа с PostgreSQL, staging‑таблицами, импортом KML и т. д.),
 * важно, чтобы:
 * - {@code folderId} указывал на фиксированную папку в Alfresco — так проще организовать
 *   предсказуемую структуру хранения файлов (например, отдельно для KML, PDF, снимков);
 * - учётные данные не хранились в коде, а подтягивались из конфига — это критично при развёртывании
 *   в разных средах (dev/stage/prod) и при работе с чувствительными данными.
 * </p>
 */
@ConfigurationProperties(prefix = "alfresco")
public record AlfrescoProperties(
        /**
         * Базовый URL экземпляра Alfresco (без завершающего слэша).
         */
        String url,

        /**
         * Имя пользователя для аутентификации в Alfresco.
         */
        String username,

        /**
         * Пароль пользователя для аутентификации в Alfresco.
         */
        String password,

        /**
         * ID корневой папки в Alfresco, куда будут загружаться файлы.
         * <p>
         * Позволяет централизованно хранить все файлы приложения в одном месте
         * и упрощает последующую очистку через {@link AlfrescoCleanupJob}.
         * </p>
         */
        String folderId
) {
}
