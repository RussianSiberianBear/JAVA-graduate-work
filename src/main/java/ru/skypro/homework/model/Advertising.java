package ru.skypro.homework.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Advertising {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="author",nullable = false)
    @ManyToOne
    private User author;

    @Column(nullable = false, unique = true, length = 32)
    private String image;

    @Column(nullable = false)
    BigDecimal price;

    @Column(nullable = false, length = 255)
    private String title;

}
