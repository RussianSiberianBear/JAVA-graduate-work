package ru.skypro.homework.constants;

/**
 * Утилитарный класс для хранения и форматирования сообщений об исключениях.
 * <p>
 * Содержит шаблоны сообщений для различных сценариев ошибок (не найдено объявление, пользователь, комментарий,
 * отказано в доступе, неверный пароль и т. п.) и методы для подстановки параметров в эти шаблоны.
 * </p>
 */
public class ExceptionMessages {

    /**
     * Шаблон сообщения: объявление с указанным ID не найдено.
     */
    private static final String AD_NOT_FOUND = "Объявление с id = {adsId} не найдено!";

    /**
     * Шаблон сообщения: пользователь не найден.
     * Примечание: в текущей реализации в шаблон подставляется username, хотя в тексте стоит {userId}.
     */
    private static final String USER_NOT_FOUND = "Пользователь {username} не найден!";

    /**
     * Шаблон сообщения: комментарий с указанным ID не найден.
     */
    private static final String COMMENT_NOT_FOUND = "Комментарий с id = {commentId} не найден!";

    /**
     * Шаблон сообщения: нет доступа к объявлению.
     */
    private static final String ACCESS_DENIED = "Нет доступа к объявлению с id = {adsId}!";

    /**
     * Сообщение: неверный текущий пароль.
     */
    private static final String INVALID_CURRENT_PASSWORD = "Неверный текущий пароль!";

    /**
     * Шаблон сообщения: файл с указанным ID не найден.
     */
    private static final String FILE_NOT_FOUND = "Файл с id = {fileId} не найден!";

    /**
     * Универсальный метод для подстановки параметров по индексу в шаблон.
     * <p>
     * Заменяет в шаблоне подстроки вида "{0}", "{1}" и т. д. на соответствующие значения из массива params.
     * Пример: format("Ошибка в элементе {0}: {1}", "поле", "пустое значение") →
     * "Ошибка в элементе поле: пустое значение".
     * </p>
     *
     * @param template шаблон строки с плейсхолдерами вида {0}, {1}, ...
     * @param params   параметры для подстановки
     * @return строка с подставленными значениями
     */
    public static String format(String template, Object... params) {
        String result = template;
        for (int i = 0; i < params.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(params[i]));
        }
        return result;
    }

    /**
     * Форматирует сообщение «Объявление не найдено» с подстановкой ID объявления.
     *
     * @param adsId ID объявления
     * @return отформатированное сообщение
     */
    public static String formatAdNotFound(Long adsId) {
        return AD_NOT_FOUND.replace("{adsId}", String.valueOf(adsId));
    }

    /**
     * Форматирует сообщение «Пользователь не найден» с подстановкой имени (логина) пользователя.
     *
     * @param username имя (логин) пользователя
     * @return отформатированное сообщение
     */
    public static String formatUserNotFound(String username) {
        return USER_NOT_FOUND.replace("{username}", username);
    }

    /**
     * Форматирует сообщение «Комментарий не найден» с подстановкой ID комментария.
     *
     * @param commentId ID комментария
     * @return отформатированное сообщение
     */
    public static String formatCommentNotFound(Long commentId) {
        return COMMENT_NOT_FOUND.replace("{commentId}", String.valueOf(commentId));
    }

    /**
     * Возвращает сообщение «Неверный текущий пароль».
     *
     * @return сообщение об ошибке
     */
    public static String invalidCurrentPassword() {
        return INVALID_CURRENT_PASSWORD;
    }

    /**
     * Форматирует сообщение «Нет доступа к объявлению» с подстановкой ID объявления.
     *
     * @param adsId ID объявления
     * @return отформатированное сообщение
     */
    public static String formatAccessDenied(Long adsId) {
        return ACCESS_DENIED.replace("{adsId}", String.valueOf(adsId));
    }
}
