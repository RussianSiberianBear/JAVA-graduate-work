package ru.skypro.homework.service;

import org.springframework.stereotype.Service;
import ru.skypro.homework.constants.ExceptionMessages;
import ru.skypro.homework.dto.CommentOneResponseDto;
import ru.skypro.homework.dto.CommentRequestDto;
import ru.skypro.homework.dto.CommentsAllResponseDto;
import ru.skypro.homework.exception.AdvertisingNotFoundException;
import ru.skypro.homework.exception.CommentNotFoundException;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.model.Advertising;
import ru.skypro.homework.model.Comment;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.AdvertisingRepository;
import ru.skypro.homework.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final AdvertisingRepository advertisingRepository;

    public CommentService(AdvertisingRepository advertisingRepository, CommentMapper commentMapper, CommentRepository commentRepository) {
        this.commentMapper = commentMapper;
        this.commentRepository = commentRepository;
        this.advertisingRepository = advertisingRepository;
    }

    public CommentsAllResponseDto findAll() {
        List<CommentOneResponseDto> commentListDto = commentRepository.findAll().stream().map(commentMapper::toResponse).toList();
        return new CommentsAllResponseDto(commentListDto.size(), commentListDto);
    }

    public CommentsAllResponseDto findByAdvertisingId(Long id) {
        List<CommentOneResponseDto> commentListDto = commentRepository.findAllByAdvertisingId(id).stream().map(commentMapper::toResponse).toList();
        advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));
        return new CommentsAllResponseDto(commentListDto.size(), commentListDto);
    }

    public CommentOneResponseDto addCommentToAdvertisingId(User user, Long id, CommentRequestDto dto) {

        Advertising ad = advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));

        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setText(dto.text());
        comment.setAdvertising(ad);
        comment.setCreatedAt(LocalDateTime.now());
        Comment commentSaved = commentRepository.save(comment);
        return commentMapper.toResponse(commentSaved);
    }

    public void deleteCommentByIdAndAdvertisingById(Long commentId, Long advertisingId) {

        advertisingRepository.findById(advertisingId)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(advertisingId)));

        Long id = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(ExceptionMessages.formatCommentNotFound(commentId))).getId();
        commentRepository.deleteById(id);
    }

    public CommentOneResponseDto updateCommentByIdAndAdvertisingById(Long commentId, Long advertisingId, CommentRequestDto dto) {

        advertisingRepository.findById(advertisingId)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(advertisingId)));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(ExceptionMessages.formatCommentNotFound(commentId)));

        comment.setText(dto.text());
        commentRepository.save(comment);
        return commentMapper.toResponse(comment);
    }

}
