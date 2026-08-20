package ru.skypro.homework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Сущность комментария к объявлению (Comment).
 * <p>
 * Соответствует таблице "advertising_comments" в базе данных. Хранит текст комментария,
 * дату создания, а также связи с объявлением и автором комментария.
 * </p>
 */
@Data
@Entity
@Table(name = "advertising_comments")
public class Comment {

    /**
     * Уникальный идентификатор комментария.
     * Генерируется автоматически базой данных (стратегия IDENTITY).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Объявление, к которому относится комментарий.
     * <p>
     * Связь ManyToOne: у одного объявления может быть много комментариев.
     * Загрузка выполняется лениво (FetchType.LAZY) для оптимизации запросов.
     * Поле помечено @JsonIgnore, чтобы не сериализовать всё объявление в JSON‑ответ.
     * Внешний ключ хранится в колонке advertising_id.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertising_id", nullable = false)
    @JsonIgnore
    private Advertising advertising;

    /**
     * Автор комментария — связь с сущностью User.
     * <p>
     * Связь ManyToOne: у одного пользователя может быть много комментариев.
     * Загрузка ленивая (FetchType.LAZY). Поле помечено @JsonIgnore, чтобы избежать
     * циклической сериализации или избыточных данных в ответе.
     * Внешний ключ хранится в колонке author_id.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    private User author;

    /**
     * Дата и время создания комментария.
     * <p>
     * Хранится в формате LocalDateTime. В DTO часто преобразуется в timestamp
     * (например, через маппер CommentMapper) для удобства фронтенда.
     * Если требуется автоматическое заполнение, можно добавить @Column(updatable = false)
     * и использовать @PrePersist в отдельном слушателе или Hibernate auditing.
     * </p>
     */
    private LocalDateTime createdAt;

    /**
     * Текст комментария.
     * <p>
     * Не имеет ограничений по длине на уровне аннотаций — в БД тип поля может быть TEXT.
     * При необходимости можно добавить @Column(length = ...) или валидацию на уровне DTO.
     * </p>
     */
    private String text;
}
