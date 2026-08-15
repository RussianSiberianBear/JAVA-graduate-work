package ru.skypro.homework.service.storage;

import java.io.InputStream;

public record FileUploadRequest(
        String directory, // Для будущих хранилищ (S3, локальное). Alfresco игнорирует
        String fileName,
        String contentType,
        long size,
        InputStream content
) {
}
