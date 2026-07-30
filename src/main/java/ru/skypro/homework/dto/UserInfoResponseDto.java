package ru.skypro.homework.dto;

public record UserInfoResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        Role role,
        String image
) {}