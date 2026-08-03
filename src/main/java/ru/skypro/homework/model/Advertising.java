package ru.skypro.homework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "advertising")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Advertising {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore  // Игнорируем при сериализации, но можно использовать для десериализации
    private User author;

    @Column(nullable = false, unique = true, length = 32)
    private String image;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false, length = 255)
    private String title;

}