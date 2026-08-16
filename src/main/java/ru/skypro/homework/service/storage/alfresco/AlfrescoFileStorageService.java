package ru.skypro.homework.service.storage.alfresco;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.skypro.homework.service.storage.FileStorageService;
import ru.skypro.homework.service.storage.FileUploadRequest;
import ru.skypro.homework.service.storage.StoredFile;
import ru.skypro.homework.service.storage.StoredFileInfo;

import java.io.IOException;

@Service
public class AlfrescoFileStorageService implements FileStorageService {

    private static final String API = "/api/-default-/public/alfresco/versions/1/nodes";

    private final RestClient client;
    private final AlfrescoProperties properties;

    public AlfrescoFileStorageService(RestClient alfrescoRestClient, AlfrescoProperties properties) {
        this.client = alfrescoRestClient;
        this.properties = properties;
    }

    @Override
    public StoredFileInfo upload(FileUploadRequest request) {
        try {
            MultipartBodyBuilder body = new MultipartBodyBuilder();

            // Читаем InputStream в byte[]
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

            return toInfo(response.entry());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content for upload: " + e.getMessage(), e);
        }
    }

    @Override
    public StoredFileInfo replace(String fileId, FileUploadRequest request) {
        try {
            byte[] contentBytes = request.content().readAllBytes();

            MultipartBodyBuilder body = new MultipartBodyBuilder();
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

            // Используем POST с параметром overwrite
            AlfrescoResponse response = client.post()
                    .uri(API + "/{folderId}/children?overwrite=true", properties.folderId())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body.build())
                    .retrieve()
                    .body(AlfrescoResponse.class);

            // Удаляем старый файл
            client.delete()
                    .uri(API + "/{id}?permanent=true", fileId)
                    .retrieve()
                    .toBodilessEntity();

            return toInfo(response.entry());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content for replace: " + e.getMessage(), e);
        }
    }

    @Override
    public StoredFileInfo getInfo(String fileId) {

        AlfrescoResponse response = client.get()
                .uri(API + "/{id}", fileId)
                .retrieve()
                .body(AlfrescoResponse.class);

        return toInfo(response.entry());
    }

    @Override
    public StoredFile get(String fileId) {

        StoredFileInfo info = getInfo(fileId);

        byte[] content = client.get()
                .uri(API + "/{id}/content", fileId)
                .retrieve()
                .body(byte[].class);

        return new StoredFile(info, content);
    }

    @Override
    public void delete(String fileId) {

        client.delete()
                .uri(API + "/{id}?permanent=true", fileId)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public boolean exists(String fileId) {
        try {
            getInfo(fileId);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    private StoredFileInfo toInfo(
            AlfrescoResponse.Entry entry
    ) {
        return new StoredFileInfo(
                entry.id(),
                entry.name(),
                entry.content().mimeType(),
                entry.content().sizeInBytes()
        );
    }
}