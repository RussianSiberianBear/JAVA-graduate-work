package ru.skypro.homework.config;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.exception.FileStorageException;

import java.util.Set;

/**
 * Валидатор файлов изображений.
 * <p>
 * Проверяет:
 * - файл не пустой;
 * - MIME-тип — только изображения (JPEG, PNG, WebP);
 * - расширение файла — только .jpg, .jpeg, .png, .webp.
 * </p>
 */
public final class ImageValidator {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            "image/jpg",
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp"
    );

    private ImageValidator() {
        // Утиль-класс, конструктор приватный
    }

    /**
     * Валидирует файл изображения.
     *
     * @param file загруженный файл
     * @throws FileStorageException если файл не проходит валидацию
     */
    public static void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Файл не может быть пустым");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new FileStorageException("Допустимы только изображения: JPEG, PNG, WebP");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasAllowedExtension(originalFilename)) {
            throw new FileStorageException("Допустимы только файлы с расширениями: jpg, jpeg, png, webp");
        }
    }

    /**
     * Проверяет расширение файла.
     */
    private static boolean hasAllowedExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return false;
        }
        String extension = filename.substring(dotIndex + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
}
