package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skypro.homework.model.Advertising;

import java.util.List;
import java.util.Optional;

public interface AdvertisingRepository extends JpaRepository<Advertising, Long> {

    List<Advertising> findAll();

    Optional<Advertising> findById(Long id);
}
