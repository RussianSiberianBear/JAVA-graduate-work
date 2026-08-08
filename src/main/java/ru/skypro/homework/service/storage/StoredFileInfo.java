package ru.skypro.homework.service.storage;

public record StoredFileInfo(
        String id,
        String fileName,
        String contentType,
        long size
) {}