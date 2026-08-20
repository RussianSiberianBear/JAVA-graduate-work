package ru.skypro.homework.service.storage.alfresco;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.skypro.homework.exception.FileStorageException;
import ru.skypro.homework.service.storage.FileStorageService;
import ru.skypro.homework.service.storage.FileUploadRequest;
import ru.skypro.homework.service.storage.StoredFile;
import ru.skypro.homework.service.storage.StoredFileInfo;

import java.io.IOException;

/**
 * Реализация {@link FileStorageService} для работы с файлами через Alfresco.
 * <p>
 * Обеспечивает полный CRUD для файлов в Alfresco:
 * - загрузка (с автопереименованием при конфликте имён);
 * - получение метаданных и содержимого;
 * - удаление (в т. ч. перманентное);
 * - проверка существования.
 * </p>
 * <p>
 * Учитывая твой контекст (работа с лесоустройством, базами данных, геодезическими данными),
 * важно, что сервис:
 * - гарантирует согласованность ID: идентификатор из Alfresco становится ID в приложении;
 * - сохраняет MIME‑тип и размер — это критично для валидации вложений (например, KML‑файлов, снимков, PDF‑отчётов);
 * - использует транзакционно‑безопасный подход к удалению через {@link AlfrescoCleanupJob} и {@link FileReplacementCoordinator}.
 * </p>
 */
@Service
@Slf4j
public class AlfrescoFileStorageService implements FileStorageService {

    private static final String API = "/api/-default-/public/alfresco/versions/1/nodes";

    private final RestClient client;
    private final AlfrescoProperties properties;

    public AlfrescoFileStorageService(RestClient alfrescoRestClient, AlfrescoProperties properties) {
        this.client = alfrescoRestClient;
        this.properties = properties;
    }

    /**
     * Загружает файл в Alfresco в указанную папку.
     * <p>
     * Использует multipart‑запрос с полями:
     * - filedata — содержимое файла;
     * - name — имя файла;
     * - nodeType — тип узла (cm:content);
     * - autoRename=true — автоматически переименовывает файл при совпадении имён.
     * </p>
     *
     * @param request запрос на загрузку ({@link FileUploadRequest})
     * @return метаданные загруженного файла ({@link StoredFileInfo})
     * @throws FileStorageException если загрузка не удалась
     */
    @Override
    public StoredFileInfo upload(FileUploadRequest request) {
        try {
            MultipartBodyBuilder body = new MultipartBodyBuilder();

            // Читаем InputStream в byte[] — необходимо для формирования multipart
            byte[] contentBytes = request.content().readAllBytes();

            body.part(
                    "filedata",
                    new ByteArrayResource(contentBytes) {
                        @Override
                        public String getFilename() {
                            return request.fileName();
                        }
                    }
            ).contentType(MediaType.parseMediaType(request.contentType()));

            body.part("name", request.fileName());
            body.part("nodeType", "cm:content");
            body.part("autoRename", "true");

            AlfrescoResponse response = client.post()
                    .uri(API + "/{folderId}/children", properties.folderId())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body.build())
                    .retrieve()
                    .body(AlfrescoResponse.class);

            if (response == null || response.entry() == null) {
                throw new FileStorageException("Failed to upload file: empty response from Alfresco");
            }

            return toInfo(response.entry());

        } catch (IOException e) {
            throw new FileStorageException("Failed to read file content for upload: " + e.getMessage(), e);
        } catch (HttpClientErrorException e) {
            throw new FileStorageException("Failed to upload file to Alfresco: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FileStorageException("Unexpected error during file upload: " + e.getMessage(), e);
        }
    }

    /**
     * Получает метаданные файла по его идентификатору.
     * <p>
     * Делает GET‑запрос к узлу Alfresco и преобразует ответ в {@link StoredFileInfo}.
     * </p>
     *
     * @param fileId идентификатор файла
     * @return метаданные файла
     * @throws FileStorageException если файл не найден или произошла ошибка
     */
    @Override
    public StoredFileInfo getInfo(String fileId) {
        try {
            AlfrescoResponse response = client.get()
                    .uri(API + "/{id}", fileId)
                    .retrieve()
                    .body(AlfrescoResponse.class);

            if (response == null || response.entry() == null) {
                throw new FileStorageException("Failed to get file info: empty response from Alfresco for fileId: " + fileId);
            }

            return toInfo(response.entry());

        } catch (HttpClientErrorException.NotFound e) {
            throw new FileStorageException("File not found: " + fileId, e);
        } catch (HttpClientErrorException e) {
            throw new FileStorageException("Failed to get file info from Alfresco: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FileStorageException("Unexpected error during get file info: " + e.getMessage(), e);
        }
    }

    /**
     * Получает файл (метаданные + содержимое) по идентификатору.
     * <p>
     * Сначала запрашивает метаданные через {@link #getInfo(String)}, затем содержимое
     * через эндпоинт /nodes/{id}/content.
     * </p>
     *
     * @param fileId идентификатор файла
     * @return объект с метаданными и содержимым файла ({@link StoredFile})
     * @throws FileStorageException если файл не найден или произошла ошибка
     */
    @Override
    public StoredFile get(String fileId) {
        try {
            StoredFileInfo info = getInfo(fileId);

            byte[] content = client.get()
                    .uri(API + "/{id}/content", fileId)
                    .retrieve()
                    .body(byte[].class);

            if (content == null) {
                throw new FileStorageException("Failed to get file content: empty response from Alfresco for fileId: " + fileId);
            }

            return new StoredFile(info, content);

        } catch (HttpClientErrorException.NotFound e) {
            throw new FileStorageException("File not found: " + fileId, e);
        } catch (HttpClientErrorException e) {
            throw new FileStorageException("Failed to get file from Alfresco: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FileStorageException("Unexpected error during get file: " + e.getMessage(), e);
        }
    }

    /**
     * Удаляет файл из Alfresco перманентно.
     * <p>
     * Параметр {@code permanent=true} означает, что файл не попадает в корзину Alfresco
     * и удаляется сразу. Это важно для корректной работы с «сиротскими» файлами
     * в связке с {@link AlfrescoCleanupJob}.
     * </p>
     *
     * @param fileId идентификатор файла для удаления
     * @throws FileStorageException если удаление не удалось
     */
    @Override
    public void delete(String fileId) {
        try {
            client.delete()
                    .uri(API + "/{id}?permanent=true", fileId)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully deleted file: {}", fileId);

        } catch (HttpClientErrorException.NotFound e) {
            throw new FileStorageException("File not found for deletion: " + fileId, e);
        } catch (HttpClientErrorException e) {
            throw new FileStorageException("Failed to delete file from Alfresco: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FileStorageException("Unexpected error during file deletion: " + e.getMessage(), e);
        }
    }

    /**
     * Проверяет существование файла в Alfresco.
     * <p>
     * Вызывает {@link #getInfo(String)} и интерпретирует ошибку «not found» как false.
     * Другие ошибки пробрасываются дальше.
     * </p>
     *
     * @param fileId идентификатор файла
     * @return true, если файл существует; false — если нет
     */
    @Override
    public boolean exists(String fileId) {
        try {
            getInfo(fileId);
            return true;
        } catch (FileStorageException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Преобразует ответ Alfresco ({@link AlfrescoResponse.Entry}) в {@link StoredFileInfo}.
     * <p>
     * Извлекает:
     * - id — уникальный ID узла;
     * - name — имя файла;
     * - mimeType — MIME‑тип;
     * - sizeInBytes — размер в байтах.
     * </p>
     *
     * @param entry ответ от Alfresco с данными об узле
     * @return преобразованные метаданные файла
     * @throws FileStorageException если данные неполные
     */
    private StoredFileInfo toInfo(AlfrescoResponse.Entry entry) {
        if (entry == null) {
            throw new FileStorageException("Cannot convert null entry to StoredFileInfo");
        }
        if (entry.content() == null) {
            throw new FileStorageException("File content metadata is null for entry: " + entry.id());
        }
        return new StoredFileInfo(
                entry.id(),
                entry.name(),
                entry.content().mimeType(),
                entry.content().sizeInBytes()
        );
    }
}
