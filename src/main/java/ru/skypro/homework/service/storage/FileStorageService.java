package ru.skypro.homework.service.storage;

public interface FileStorageService {

    StoredFileInfo upload(FileUploadRequest request);

    StoredFileInfo getInfo(String fileId);

    StoredFile get(String fileId);

    StoredFileInfo replace(
            String fileId,
            FileUploadRequest request
    );

    void delete(String fileId);

    boolean exists(String fileId);
}