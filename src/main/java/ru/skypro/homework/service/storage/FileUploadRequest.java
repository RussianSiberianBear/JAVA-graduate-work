package ru.skypro.homework.service.storage;

import java.io.InputStream;

public record FileUploadRequest(
        String directory,
        String fileName,
        String contentType,
        long size,
        InputStream content
) {
}
