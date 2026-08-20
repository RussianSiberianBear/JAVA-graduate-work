package ru.skypro.homework.service.storage.alfresco;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO для парсинга ответов Alfresco API при операциях с узлами (загрузка, получение метаданных).
 * <p>
 * Отражает структуру JSON‑ответа от Alfresco REST API для эндпоинтов работы с узлами.
 * Аннотация {@link JsonIgnoreProperties} делает маппинг устойчивым к изменениям API
 * и дополнительным метаданным, которые не нужны в бизнес‑логике.
 * </p>
 * <p>
 * - {@code id} —для сохранения ссылки на файл в БД ;
 * - {@code mimeType} — тип файла;
 * - {@code sizeInBytes} — размер файла в байтах
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AlfrescoResponse(Entry entry) {

    /**
     * Метаданные узла (файла или папки) в Alfresco.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entry(
            /**
             * Уникальный идентификатор узла в Alfresco (NodeRef).
             * Используется как fileId в приложении и хранится в БД.
             */
            String id,

            /**
             * Имя узла (отображаемое имя файла или папки).
             */
            String name,

            /**
             * Метаданные содержимого (MIME‑тип, размер).
             * Для папок может быть null—это обрабатывается в {@link AlfrescoFileStorageService#toInfo}.
             */
            Content content
    ) {
    }

    /**
     * Метаданные содержимого файла.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(
            /**
             * MIME‑тип файла (например, application/vnd.google-earth.kml+xml, image/png, application/pdf).
             *
             */
            String mimeType,

            /**
             * Размер файла в байтах.
             * Может быть полезен для:
             * - проверки лимитов;
             * - отображения в UI;
             * - оценки нагрузки на сеть и хранилище;
             * - логирования и мониторинга.
             */
            Long sizeInBytes
    ) {
    }
}
