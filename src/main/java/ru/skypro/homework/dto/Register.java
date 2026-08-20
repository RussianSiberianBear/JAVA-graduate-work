package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * DTO для запроса на регистрацию нового пользователя.
 * <p>
 * Используется в POST‑запросе к эндпоинту /register. Содержит все необходимые данные
 * для создания учётной записи: логин (email), пароль, имя, фамилию, телефон и роль.
 * Валидация выполняется с помощью аннотаций Jakarta Validation; также предусмотрен
 * компактный конструктор для дополнительной проверки на null/пустые значения.
 * </p>
 */
@Schema(description = "Запрос на регистрацию нового пользователя")
public record Register(

        /**
         * Логин пользователя (используется как email).
         * Обязательное поле, должно быть непустым, соответствовать формату email,
         * не превышать 32 символа и удовлетворять заданному регулярному выражению.
         * Пример: john.doe@example.com
         */
        @Schema(description = "Логин пользователя (используется как email)",
                example = "john.doe@example.com",
                required = true,
                minLength = 4,
                maxLength = 32,
                pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        @NotBlank(message = "Логин пользователя не может быть пустым!")
        @Email(message = "Логин должен быть корректным email‑адресом")
        @Size(max = 32, message = "Логин не может превышать 32 символа")
        String username,

        /**
         * Пароль пользователя.
         * Обязательное поле: от 8 до 16 символов, должно содержать цифру, заглавную
         * и строчную букву, специальный символ из набора @#$%^&+=, не должно содержать пробелов.
         * Пример: SecurePass123!
         */
        @Schema(description = "Пароль пользователя. Должен содержать минимум 8 символов, " +
                "одну цифру, одну заглавную и одну строчную букву",
                example = "SecurePass123!",
                required = true,
                minLength = 8,
                maxLength = 16,
                pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")
        @NotBlank(message = "Пароль не может быть пустым!")
        @Size(min = 8, max = 16, message = "Пароль должен содержать от 8 до 16 символов")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
                message = "Пароль должен содержать минимум 8 символов, " +
                        "одну цифру, одну заглавную и одну строчную букву, " +
                        "один специальный символ (@#$%^&+=), и не содержать пробелов"
        )
        String password,

        /**
         * Имя пользователя.
         * Обязательное поле: от 2 до 16 символов, может содержать только буквы, пробелы и дефисы.
         * Пример: John
         */
        @Schema(description = "Имя пользователя",
                example = "John",
                required = true,
                minLength = 2,
                maxLength = 16,
                pattern = "^[A-Za-zА-Яа-я\\s-]+$")
        @NotBlank(message = "Имя пользователя не может быть пустым!")
        @Size(min = 2, max = 16, message = "Имя должно содержать от 2 до 16 символов")
        @Pattern(regexp = "^[A-Za-zА-Яа-я\\s-]+$",
                message = "Имя может содержать только буквы, пробелы и дефисы")
        String firstName,

        /**
         * Фамилия пользователя.
         * Обязательное поле: от 2 до 16 символов, может содержать только буквы, пробелы и дефисы.
         * Пример: Doe
         */
        @Schema(description = "Фамилия пользователя",
                example = "Doe",
                required = true,
                minLength = 2,
                maxLength = 16,
                pattern = "^[A-Za-zА-Яа-я\\s-]+$")
        @NotBlank(message = "Фамилия пользователя не может быть пустой!")
        @Size(min = 2, max = 16, message = "Фамилия должна содержать от 2 до 16 символов")
        @Pattern(regexp = "^[A-Za-zА-Яа-я\\s-]+$",
                message = "Фамилия может содержать только буквы, пробелы и дефисы")
        String lastName,

        /**
         * Номер телефона пользователя.
         * Обязательное поле: от 10 до 20 символов, может начинаться с «+», допускаются цифры,
         * пробелы, дефисы и скобки.
         * Пример: +7 (999) 123-45-67
         */
        @Schema(description = "Номер телефона пользователя",
                example = "+7 (999) 123-45-67",
                required = true,
                pattern = "^\\+?[0-9\\s\\-()]{10,20}$")
        @NotBlank(message = "Телефон пользователя не может быть пустым!")
        @Pattern(regexp = "^\\+?[0-9\\s\\-()]{10,20}$",
                message = "Телефон должен содержать от 10 до 20 цифр, может начинаться с +")
        String phone,

        /**
         * Роль пользователя в системе.
         * Обязательное поле; допустимые значения: USER, ADMIN.
         * Пример: USER
         */
        @Schema(description = "Роль пользователя в системе",
                example = "USER",
                required = true,
                allowableValues = {"USER", "ADMIN"})
        @NotNull(message = "Роль пользователя не может быть пустым значением!")
        Role role

) {

    /**
     * Компактный конструктор record, выполняющий дополнительную проверку на null и пустые значения.
     * Эта проверка дублирует часть валидации, заданной аннотациями, и служит дополнительной защитой
     * от некорректных данных на уровне создания экземпляра.
     */
    public Register {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Логин пользователя не может быть пустым!");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Пароль не может быть пустым!");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым!");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Фамилия пользователя не может быть пустой!");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Телефон пользователя не может быть пустым!");
        }
        if (role == null) {
            throw new IllegalArgumentException("Роль пользователя не может быть пустым значением!");
        }
    }
}
