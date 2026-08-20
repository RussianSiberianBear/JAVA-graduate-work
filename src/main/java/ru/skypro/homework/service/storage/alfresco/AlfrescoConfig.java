package ru.skypro.homework.service.storage.alfresco;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

/**
 * Конфигурация для взаимодействия с Alfresco API.
 * <p>
 * Создаёт бин {@link RestClient}, настроенный на работу с Alfresco:
 * - базовый URL берётся из {@link AlfrescoProperties};
 * - автоматически добавляет заголовок Authorization (Basic Auth) ко всем запросам.
 * </p>
 */
@Configuration
@EnableConfigurationProperties(AlfrescoProperties.class)
public class AlfrescoConfig {

    /**
     * Создаёт и настраивает {@link RestClient} для запросов к Alfresco.
     * <p>
     * Клиент:
     * - использует базовый URL из свойств конфигурации;
     * - добавляет заголовок Basic Authorization на основе username/password из свойств;
     * - готов к немедленному использованию в сервисах и джобах (например, в {@link AlfrescoCleanupJob}).
     * </p>
     *
     * @param properties свойства подключения к Alfresco ({@link AlfrescoProperties})
     * @return настроенный экземпляр {@link RestClient}
     */
    @Bean
    public RestClient alfrescoRestClient(
            AlfrescoProperties properties
    ) {
        return RestClient.builder()
                .baseUrl(properties.url())
                .defaultRequest(request -> {
                    // Формируем строку для Basic Auth: "username:password"
                    String auth = properties.username() + ":" + properties.password();

                    // Кодируем в Base64
                    String encodedAuth = java.util.Base64.getEncoder()
                            .encodeToString(auth.getBytes());

                    // Добавляем заголовок Authorization
                    request.headers(headers ->
                            headers.add(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                    );
                })
                .build();
    }
}
