package ru.skypro.homework.dto;

public record SetPasswordResponseDto(
        String currentPassword,
        String newPassword
) {}
