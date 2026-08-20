package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.skypro.homework.model.Comment;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Comment}.
 * <p>
 * Расширяет {@link JpaRepository}, предоставляя базовые CRUD‑операции,
 * а также содержит дополнительные методы для выборки комментариев:
 * - получение всех комментариев к конкретному объявлению с данными об авторе;
 * - поиск комментария по его ID и привязке к объявлению (для проверки принадлежности).
 * </p>
 */
public interface CommentRepository
        extends JpaRepository<Comment, Long> {

    /**
     * Возвращает список всех комментариев к указанному объявлению вместе с данными об авторе.
     * <p>
     * Загрузка поля author выполняется сразу (eager) за счёт {@link EntityGraph},
     * что позволяет избежать проблемы N+1 при обращении к author после выборки.
     * </p>
     *
     * @param advertisingId идентификатор объявления
     * @return список комментариев к объявлению; пустой список, если комментариев нет
     */
    @EntityGraph(attributePaths = "author")
    List<Comment> findAllByAdvertisingId(Long advertisingId);

    /**
     * Находит комментарий по его идентификатору и проверке привязки к конкретному объявлению.
     * <p>
     * Такой запрос полезен, когда нужно убедиться, что комментарий действительно относится
     * к данному объявлению (например, при обновлении или удалении комментария через REST API).
     * Если комментарий не найден либо не принадлежит указанному объявлению, возвращается пустой Optional.
     * </p>
     *
     * @param commentId      идентификатор комментария
     * @param advertisingId  идентификатор объявления, к которому должен относиться комментарий
     * @return Optional с найденным комментарием либо пустой Optional
     */
    Optional<Comment> findByIdAndAdvertisingId(
            Long commentId,
            Long advertisingId
    );
}
