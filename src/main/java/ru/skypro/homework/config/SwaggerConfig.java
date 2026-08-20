package ru.skypro.homework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Swagger/OpenAPI для API приложения.
 * <p>
 * Определяет метаданные API (название, версия, описание, контакт), а также настраивает
 * схему безопасности (Basic Authentication). Конфигурация регистрируется как Spring-бин
 * и автоматически подхватывается стартером Swagger UI.
 * </p>
 */
@Configuration
public class SwaggerConfig {

    /**
     * Создаёт и настраивает экземпляр {@link OpenAPI} с метаданными и схемой безопасности.
     *
     * @return сконфигурированный объект {@link OpenAPI}
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Homework API")
                        .version("1.0")
                        .description("API для дипломной работы по курсу JAVA-разработчик")
                        .contact(new Contact()
                                .name("Student")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("basicAuth", new SecurityScheme()
                                .name("basicAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("Basic Authentication")));
    }
}
