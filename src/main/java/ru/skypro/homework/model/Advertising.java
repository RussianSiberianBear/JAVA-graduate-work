package ru.skypro.homework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Сущность объявления (Advertising) для хранения данных о публикациях в системе.
 * <p>
 * Соответствует таблице "advertising" в базе данных. Содержит основные атрибуты
 * объявления: заголовок, описание, цену, ссылку на изображение, а также связь
 * с автором (пользователем).
 * </p>
 */
@Data
@Entity
@Table(name = "advertising")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Advertising {

    /**
     * Уникальный идентификатор объявления.
     * Генерируется автоматически базой данных (стратегия IDENTITY).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Автор объявления — связь с сущностью User.
     * <p>
     * Используется ManyToOne: у одного автора может быть много объявлений.
     * Загрузка выполняется лениво (FetchType.LAZY) для оптимизации запросов.
     * Поле помечено @JsonIgnore, чтобы не сериализовать автора целиком в JSON‑ответ.
     * Внешний ключ хранится в колонке author_id.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    private User author;

    /**
     * Идентификатор файла изображения объявления в хранилище.
     * <p>
     * Значение должно быть уникальным (unique = true) и не может быть пустым.
     * Длина ограничена 255 символами. На основе этого ID мапперы формируют URL вида
     * "/images/{imageFileId}" для передачи клиенту.
     * </p>
     */
    @Column(nullable = false, unique = true, length = 255)
    private String imageFileId;

    /**
     * Цена объявления.
     * Обязательное поле, не может быть null.
     */
    @Column(nullable = false)
    private Integer price;

    /**
     * Заголовок объявления.
     * Обязательное поле, длина ограничена 255 символами.
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Описание объявления.
     * Обязательное поле, длина ограничена 255 символами.
     * (При необходимости можно увеличить длину или заменить на TEXT‑тип в БД.)
     */
    @Column(nullable = false, length = 255)
    private String description;
}
