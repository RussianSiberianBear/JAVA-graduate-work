package ru.skypro.homework.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.service.storage.FileStorageService;
import ru.skypro.homework.service.storage.StoredFile;

import java.util.concurrent.TimeUnit;

/**
 * Контроллер для работы с изображениями.
 * <p>
 * Предоставляет REST‑эндпоинт для получения файлов изображений по их идентификатору.
 * Контроллер устанавливает соответствующие HTTP‑заголовки: тип контента, кэширование и
 * заголовок Content‑Disposition для корректного отображения файла в браузере.
 * </p>
 */
@RestController
@RequestMapping("/images")
public class ImageController {

    private final FileStorageService fileStorageService;

    /**
     * Конструктор контроллера.
     *
     * @param fileStorageService сервис для работы с файловым хранилищем
     */
    public ImageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Получает изображение по его идентификатору и возвращает его в виде байтового массива.
     * <p>
     * Устанавливает следующие заголовки ответа:
     * - Content‑Type—на основе типа контента, хранящегося в метаданных файла.
     * - Cache‑Control—кэширование на 30 дней, публичное (доступно для прокси и CDN).
     * - Content‑Disposition—режим inline и имя файла для корректного рендеринга в браузере.
     * </p>
     *
     * @param fileId идентификатор запрашиваемого файла
     * @return {@link ResponseEntity} с содержимым файла, соответствующими заголовками и статусом 200
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileId) {
        StoredFile storedFile = fileStorageService.get(fileId);

        if (storedFile == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(storedFile.info().contentType()))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + storedFile.info().fileName() + "\"")
                .body(storedFile.content());
    }
}
