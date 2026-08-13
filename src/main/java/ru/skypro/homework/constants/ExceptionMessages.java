package ru.skypro.homework.constants;

public class ExceptionMessages {

    private static final String AD_NOT_FOUND = "Объявление с id = {adsId} не найдено!";
    private static final String USER_NOT_FOUND = "Пользователь {userId} не найден!";
    private static final String COMMENT_NOT_FOUND = "Комментарий с id = {commentId} не найден!";
    private static final String ACCESS_DENIED = "Нет доступа к объявлению с id = {adsId}!";
    private static final String FILE_NOT_FOUND = "Файл с id = {fileId} не найден!";

    public static String format(String template, Object... params) {
        String result = template;
        for (int i = 0; i < params.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(params[i]));
        }
        return result;
    }

    public static String formatAdNotFound(Long adsId) {
        return AD_NOT_FOUND.replace("{adsId}", String.valueOf(adsId));
    }

    public static String formatUserNotFound(String username) {
        return USER_NOT_FOUND.replace("{username}", username);
    }

    public static String formatCommentNotFound(Long commentId) {
        return COMMENT_NOT_FOUND.replace("{commentId}", String.valueOf(commentId));
    }

    public static String formatAccessDenied(Long adsId) {
        return ACCESS_DENIED.replace("{adsId}", String.valueOf(adsId));
    }
}