package ru.skypro.homework.service.storage.alfresco;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.skypro.homework.config.AlfrescoProperties;
import ru.skypro.homework.service.storage.FileStorageService;
import ru.skypro.homework.service.storage.FileUploadRequest;
import ru.skypro.homework.service.storage.StoredFile;
import ru.skypro.homework.service.storage.StoredFileInfo;

@Service
public class AlfrescoFileStorageService implements FileStorageService {

    private static final String API =
            "/api/-default-/public/alfresco/versions/1/nodes";

    private final RestClient client;
    private final AlfrescoProperties properties;

    public AlfrescoFileStorageService(RestClient alfrescoRestClient, AlfrescoProperties properties) {
        this.client = alfrescoRestClient;
        this.properties = properties;
    }

    @Override
    public StoredFileInfo upload(FileUploadRequest request) {

        MultipartBodyBuilder body = new MultipartBodyBuilder();

        body.part(
                "filedata",
                new InputStreamResource(request.content()) {
                    @Override
                    public String getFilename() {
                        return request.fileName();
                    }
                }
        ).contentType(MediaType.parseMediaType(
                request.contentType()
        ));

        body.part("name", request.fileName());
        body.part("nodeType", "cm:content");
        body.part("autoRename", "true");

        AlfrescoResponse response = client.post()
                .uri(
                        API + "/{folderId}/children",
                        properties.folderId()
                )
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body.build())
                .retrieve()
                .body(AlfrescoResponse.class);

        return toInfo(response.entry());
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
    public StoredFileInfo replace(
            String fileId,
            FileUploadRequest request
    ) {
        client.put()
                .uri(API + "/{id}/content", fileId)
                .contentType(MediaType.parseMediaType(
                        request.contentType()
                ))
                .body(new InputStreamResource(request.content()))
                .retrieve()
                .toBodilessEntity();

        return getInfo(fileId);
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