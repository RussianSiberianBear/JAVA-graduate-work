package ru.skypro.homework.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .defaultHeaders(headers ->
                        headers.setBasicAuth(
                                properties.username(),
                                properties.password()
                        )
                )
                .build();
    }
}
