package ru.skypro.homework.service;

import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.CommentOneResponseDto;
import ru.skypro.homework.dto.CommentsAllResponseDto;
import ru.skypro.homework.exception.AdvertisingNotFoundException;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.model.Advertising;
import ru.skypro.homework.repository.AdvertisingRepository;
import ru.skypro.homework.repository.CommentRepository;

import java.util.List;

@Service
public class CommentService {
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final AdvertisingRepository advertisingRepository;

    public CommentService(AdvertisingRepository advertisingRepository, CommentMapper commentMapper, CommentRepository commentRepository, AdvertisingRepository advertisingRepository1) {
        this.commentMapper = commentMapper;
        this.commentRepository = commentRepository;
        this.advertisingRepository = advertisingRepository1;
    }

    public CommentsAllResponseDto findAll() {
        List<CommentOneResponseDto> commentListDto = commentRepository.findAll().stream().map(commentMapper::toResponse).toList();
        return new CommentsAllResponseDto(commentListDto.size(), commentListDto);
    }

    public CommentsAllResponseDto findByAdvertisingId(Long id) {
        List<CommentOneResponseDto> commentListDto = commentRepository.findAllByAdvertisingId(id).stream().map(commentMapper::toResponse).toList();
        advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException("Объявление с идентификатором id= " + id + " не найдено!"));
        return new CommentsAllResponseDto(commentListDto.size(), commentListDto);
    }
}
