package ru.skypro.homework.service.storage.alfresco;

import java.time.OffsetDateTime;

public class AlfrescoFileInfo {

    private String id;
    private String name;
    private boolean isFolder;
    private OffsetDateTime createdAt;

    public AlfrescoFileInfo() {
    }

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