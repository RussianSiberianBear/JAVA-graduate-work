package ru.skypro.homework.service.storage.alfresco;

public class AlfrescoFileInfo {
    private String id;
    private String name;
    private boolean isFolder;

    public AlfrescoFileInfo() {
    }

    public AlfrescoFileInfo(String id, String name, boolean isFolder) {
        this.id = id;
        this.name = name;
        this.isFolder = isFolder;
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
}
