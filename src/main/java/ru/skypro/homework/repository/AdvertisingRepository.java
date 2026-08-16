package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.skypro.homework.model.Advertising;

import java.util.List;

public interface AdvertisingRepository extends JpaRepository<Advertising, Long> {

    List<Advertising> findAllByAuthorId(Long authorId);

    @Query("""
       select a.imageFileId
       from Advertising a
       where a.imageFileId is not null
       """)
    List<String> findAllImageFileIds();

}
