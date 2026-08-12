package ru.skypro.homework.service.storage.alfresco;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AlfrescoResponse(Entry entry) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entry(
            String id,
            String name,
            Content content
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(
            String mimeType,
            Long sizeInBytes
    ) {
    }
}