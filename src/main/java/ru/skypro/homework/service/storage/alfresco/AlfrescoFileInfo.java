package ru.skypro.homework.service.storage.alfresco;

import java.time.OffsetDateTime;

/**
 * DTO для хранения информации об узле (файле или папке) в Alfresco.
 * <p>
 * Используется внутри {@link AlfrescoCleanupJob} для временного представления
 * данных, полученных из Alfresco API, в удобной для бизнес‑логики форме.
 * </p>
 */
public class AlfrescoFileInfo {

    private String id;
    private String name;
    private boolean isFolder;
    private OffsetDateTime createdAt;

    /**
     * Конструктор по умолчанию.
     * <p>
     * Требуется для некоторых фреймворков (например, сериализаторов),
     * хотя в текущем коде используется преимущественно параметризованный конструктор.
     * </p>
     */
    public AlfrescoFileInfo() {
    }

    /**
     * Конструктор с параметрами.
     *
     * @param id        уникальный идентификатор узла в Alfresco
     * @param name      имя узла (файла или папки)
     * @param isFolder  признак того, что узел является папкой
     * @param createdAt дата и время создания узла
     */
    public AlfrescoFileInfo(
            String id,
            String name,
            boolean isFolder,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.isFolder = isFolder;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFolder() {
        return isFolder;
    }

    /**
     * Сеттер для признака папки.
     * <p>
     * Обратите внимание: метод назван {@code setFolder}, чтобы соответствовать
     * стилю геттера {@code isFolder()} и избежать конфликта имён.
     * </p>
     *
     * @param folder значение признака (true — папка, false — файл)
     */
    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
