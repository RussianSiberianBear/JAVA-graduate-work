package ru.skypro.homework.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.service.storage.FileStorageService;
import ru.skypro.homework.service.storage.StoredFile;
import org.springframework.http.CacheControl;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final FileStorageService fileStorageService;

    public ImageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileId) {
        StoredFile storedFile = fileStorageService.get(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        storedFile.info().contentType()
                ))
                .cacheControl(
                        CacheControl.maxAge(1, TimeUnit.DAYS)
                                .cachePublic()
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                storedFile.info().fileName() +
                                "\""
                )
                .body(storedFile.content());
    }
}