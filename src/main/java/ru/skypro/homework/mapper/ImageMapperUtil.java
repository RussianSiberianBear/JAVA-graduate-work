package ru.skypro.homework.mapper;

import org.mapstruct.Named;

/**
 * Утиль-класс для MapStruct-маппинга изображений.
 * <p>
 * Содержит общие методы для преобразования идентификаторов файлов
 * в URL изображений. Подключается через {@code @Mapper(uses = ImageMapperUtil.class)}
 * в мапперах, которые используют {@link #toImageUrl(String)}.
 * </p>
 */
public final class ImageMapperUtil {

    private ImageMapperUtil() {
        // Утиль-класс, конструктор приватный
    }

    /**
     * Преобразует идентификатор файла в URL изображения.
     * <p>
     * Если fileId равен null, возвращает null. Иначе формирует путь вида "/images/{fileId}".
     * </p>
     *
     * @param fileId идентификатор файла изображения
     * @return сформированный URL изображения или null, если идентификатор отсутствует
     */
    @Named("toImageUrl")
    public static String toImageUrl(String fileId) {
        return fileId == null ? null : "/images/" + fileId;
    }
}
