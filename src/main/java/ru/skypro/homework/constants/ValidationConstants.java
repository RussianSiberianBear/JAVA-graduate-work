package ru.skypro.homework.constants;

public final class ValidationConstants {

    private ValidationConstants() {
        // Приватный конструктор для предотвращения инстанцирования
    }

    // Парольные константы
    public static final String PASSWORD_REGEX =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

    public static final String PASSWORD_MESSAGE =
            "Пароль должен содержать минимум 8 символов, " +
                    "одну цифру, одну заглавную и одну строчную букву, " +
                    "один специальный символ (@#$%^&+=), и не содержать пробелов";

    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 16;

    // Имя/Фамилия константы
    public static final String USERNAME_REGEX = "^[A-Za-zА-Яа-я\\s-]+$";
    public static final String USERNAME_MESSAGE =
            "Имя может содержать только буквы, пробелы и дефисы";
    public static final int USERNAME_MIN = 2;
    public static final int USERNAME_MAX = 16;

    // Телефонные константы
    public static final String PHONE_REGEX = "^\\+?[0-9\\s\\-()]{10,20}$";
    public static final String PHONE_MESSAGE =
            "Телефон должен содержать от 10 до 20 символов, может начинаться с +, " +
                    "допускаются цифры, пробелы, дефисы и скобки";
    public static final int PHONE_MIN = 10;
    public static final int PHONE_MAX = 20;

    // Email константы (опционально, для будущего использования)
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String EMAIL_MESSAGE = "Логин должен быть корректным email‑адресом";
    public static final int EMAIL_MAX = 32;
}