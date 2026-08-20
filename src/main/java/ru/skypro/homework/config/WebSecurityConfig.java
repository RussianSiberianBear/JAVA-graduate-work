package ru.skypro.homework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Конфигурация безопасности Spring Security для приложения.
 * <p>
 * Настраивает цепочку фильтров безопасности ({@link SecurityFilterChain}), отключает CSRF,
 * включает CORS, определяет правила авторизации для различных эндпоинтов и устанавливает
 * схему аутентификации HTTP Basic. Также предоставляет бин {@link PasswordEncoder} на основе BCrypt.
 * </p>
 */
@Configuration
public class WebSecurityConfig {

    /**
     * Массив путей, доступных без авторизации (публичные эндпоинты).
     * Включает документацию Swagger, статические ресурсы, страницы входа/регистрации,
     * а также доступ к изображениям.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/login",
            "/register",
            "/images/**"
    };

    /**
     * Создаёт и настраивает цепочку фильтров безопасности.
     * <p>
     * Правила авторизации:
     * - Публичные эндпоинты из {@link #PUBLIC_ENDPOINTS} доступны всем.
     * - GET‑запросы к /ads (список объявлений) также доступны без авторизации.
     * - Все остальные запросы требуют наличия роли USER или ADMIN.
     * </p>
     *
     * @param http объект конфигурации {@link HttpSecurity}
     * @return сконфигурированный {@link SecurityFilterChain}
     * @throws Exception в случае ошибки при построении цепочки фильтров
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Отключаем CSRF (актуально для stateless API)
                .cors(withDefaults()) // Включаем поддержку CORS с настройками по умолчанию
                .authorizeHttpRequests(authorization -> authorization
                        // Публичные служебные эндпоинты, регистрация, логин и изображения
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // По контракту frontend список всех объявлений доступен без авторизации
                        .requestMatchers(HttpMethod.GET, "/ads").permitAll()

                        // Остальные операции требуют наличия роли USER или ADMIN
                        .anyRequest().hasAnyRole("USER", "ADMIN")
                )
                .httpBasic(withDefaults()); // Включаем HTTP Basic аутентификацию

        return http.build();
    }

    /**
     * Предоставляет реализацию {@link PasswordEncoder} на основе алгоритма BCrypt.
     * Используется для кодирования паролей пользователей и проверки их корректности.
     *
     * @return экземпляр {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
