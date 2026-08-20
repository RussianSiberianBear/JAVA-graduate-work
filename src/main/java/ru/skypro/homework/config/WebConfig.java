package ru.skypro.homework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация CORS (Cross‑Origin Resource Sharing) для веб‑приложения.
 * <p>
 * Настраивает правила доступа с других источников (origin) к API приложения.
 * Разрешает запросы с http://localhost:3000, все основные HTTP‑методы, любые заголовки
 * и передачу учётных данных (cookies, авторизации и т. д.).
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Настраивает маппинг CORS для всех эндпоинтов приложения.
     *
     * @param registry экземпляр {@link CorsRegistry}, используемый для регистрации правил CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Применяем ко всем эндпоинтам
                .allowedOrigins("http://localhost:3000") // Разрешённые источники (фронтенд)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // Разрешённые HTTP‑методы
                .allowedHeaders("*") // Разрешённые заголовки
                .allowCredentials(true); // Разрешить передачу учётных данных (cookies и т. п.)
    }
}
