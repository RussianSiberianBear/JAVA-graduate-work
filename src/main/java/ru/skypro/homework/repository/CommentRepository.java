package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skypro.homework.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByAdvertisingId(Long advertisingId);

    Optional<Comment> findByIdAndAdvertisingId(Long commentId, Long advertisingId);

}
