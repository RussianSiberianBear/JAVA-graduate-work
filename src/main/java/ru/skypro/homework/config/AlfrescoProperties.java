package ru.skypro.homework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "alfresco")
public record AlfrescoProperties(
        String url,
        String username,
        String password,
        String folderId
) {}
