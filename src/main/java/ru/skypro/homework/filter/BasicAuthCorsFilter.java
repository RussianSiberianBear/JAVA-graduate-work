package ru.skypro.homework.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр для настройки CORS с поддержкой учётных данных (credentials).
 * <p>
 * Применяется ко всем запросам один раз за запрос (благодаря наследованию от
 * {@link OncePerRequestFilter}). Добавляет заголовок
 * "Access-Control-Allow-Credentials: true", необходимый для работы аутентификации
 * по куки или HTTP‑авторизации в кросс‑доменных запросах (например, когда фронтенд
 * находится на другом порту или домене).
 * </p>
 */
@Component
public class BasicAuthCorsFilter extends OncePerRequestFilter {

    /**
     * Основная логика фильтра: добавляет заголовок для поддержки учётных данных
     * и передаёт запрос дальше по цепочке фильтров.
     *
     * @param httpServletRequest  входящий HTTP‑запрос
     * @param httpServletResponse исходящий HTTP‑ответ
     * @param filterChain         цепочка фильтров для продолжения обработки запроса
     * @throws ServletException в случае ошибки обработки сервлетом
     * @throws IOException      в случае ошибки ввода‑вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        httpServletResponse.addHeader("Access-Control-Allow-Credentials", "true");
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
