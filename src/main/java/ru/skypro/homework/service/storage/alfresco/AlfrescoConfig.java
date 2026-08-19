package ru.skypro.homework.service.storage.alfresco;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AlfrescoProperties.class)
public class AlfrescoConfig {

    @Bean
    public RestClient alfrescoRestClient(
            AlfrescoProperties properties
    ) {
        return RestClient.builder()
                .baseUrl(properties.url())
                .defaultRequest(request -> {
                    // Создаём строку для Basic Auth
                    String auth = properties.username() + ":" + properties.password();
                    String encodedAuth = java.util.Base64.getEncoder()
                            .encodeToString(auth.getBytes());

                    request.headers(headers ->
                            headers.add(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                    );
                })
                .build();
    }
}
