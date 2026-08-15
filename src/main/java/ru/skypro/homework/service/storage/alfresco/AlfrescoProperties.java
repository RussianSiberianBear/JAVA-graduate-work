package ru.skypro.homework.service.storage.alfresco;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "alfresco")
public record AlfrescoProperties(
        String url,
        String username,
        String password,
        String folderId
) {
}
