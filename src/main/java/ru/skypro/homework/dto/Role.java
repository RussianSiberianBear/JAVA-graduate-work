package ru.skypro.homework.dto;


public enum Role {
    USER, ADMIN;
    public int getIndex() {
        return ordinal();
    }

    public static Role getByIndex(int index) {
        if (index < 0 || index >= values().length) {
            throw new IllegalArgumentException(
                    "Недопустимый индекс: " + index +
                            ". Должен быть между 0  и " + (values().length - 1));
        }
        return values()[index];
    }

}
