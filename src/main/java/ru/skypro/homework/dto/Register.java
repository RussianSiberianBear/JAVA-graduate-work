package ru.skypro.homework.dto;

public record Register(
        String username,
        String password,
        String firstName,
        String lastName,
        String phone,
        Role role
) {
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
            throw new IllegalArgumentException("Фамилия пользователя не может быть пустым!");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Телефон пользователя не может быть пустым!");
        }
        if (role == null) {
            throw new IllegalArgumentException("Роль пользователя не может быть пустым значением!");
        }
    }
}
