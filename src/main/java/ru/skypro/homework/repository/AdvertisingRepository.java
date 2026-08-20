package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.skypro.homework.model.Advertising;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Advertising}.
 * <p>
 * Расширяет {@link JpaRepository}, предоставляя базовые CRUD‑операции,
 * а также содержит дополнительные методы для специфических запросов:
 * - получение объявлений автора;
 * - выборка ID изображений;
 - получение объявления вместе с автором (с оптимизацией загрузки через EntityGraph).
 * </p>
 */
public interface AdvertisingRepository
        extends JpaRepository<Advertising, Long> {

    /**
     * Возвращает список всех объявлений указанного автора.
     *
     * @param authorId идентификатор автора (пользователя)
     * @return список объявлений, принадлежащих автору; пустой список, если таких нет
     */
    List<Advertising> findAllByAuthorId(Long authorId);

    /**
     * Возвращает список идентификаторов файлов изображений для всех объявлений,
     * у которых изображение задано (imageFileId не null).
     * <p>
     * Используется, например, для массовой очистки хранилища изображений
     * при удалении объявлений или для аудита загруженных файлов.
     * </p>
     *
     * @return список imageFileId (строк), не содержащих null
     */
    @Query("""
            select a.imageFileId
            from Advertising a
            where a.imageFileId is not null
            """)
    List<String> findAllImageFileIds();

    /**
     * Находит объявление по ID вместе с данными об авторе.
     * <p>
     * Загрузка поля author выполняется сразу (eager) за счёт {@link EntityGraph},
     * что позволяет избежать проблемы N+1 при обращении к author после выборки.
     * Если объявление не найдено, возвращается пустой Optional.
     * </p>
     *
     * @param id идентификатор объявления
     * @return Optional с найденным объявлением (включая автора) либо пустой Optional
     */
    @EntityGraph(attributePaths = "author")
    Optional<Advertising> findWithAuthorById(Long id);
}
