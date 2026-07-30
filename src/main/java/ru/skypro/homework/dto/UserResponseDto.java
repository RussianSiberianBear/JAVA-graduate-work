package ru.skypro.homework.dto;

public record UserResponseDto(
        Long id,
        String email,
        String username,
        String firstName,
        String lastName,
        String phone,
        Role role,
        String image
) {}