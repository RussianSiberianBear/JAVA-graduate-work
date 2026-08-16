package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skypro.homework.model.Advertising;

import java.util.List;

public interface AdvertisingRepository extends JpaRepository<Advertising, Long> {

    List<Advertising> findAllByAuthorId(Long authorId);

}
