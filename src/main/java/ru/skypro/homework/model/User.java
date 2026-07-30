package ru.skypro.homework.model;

import jakarta.persistence.*;
import lombok.Data;
import ru.skypro.homework.dto.Role;

@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 32)
    private String email;
    @Column(nullable = false, unique = true, length = 32)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, length = 32)
    private String firstName;
    @Column(nullable = false, length = 32)
    private String lastName;
    @Column(nullable = false, length = 32)
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Role role = Role.USER;
    @Column(nullable = false, unique = true, length = 32)
    private String image;
}
