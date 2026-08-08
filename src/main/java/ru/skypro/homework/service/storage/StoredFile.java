package ru.skypro.homework.service.storage;

public record StoredFile(
        StoredFileInfo info,
        byte[] content
) {}