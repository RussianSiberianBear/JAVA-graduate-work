package ru.skypro.homework.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.skypro.homework.security.SecurityHelper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для работы с комментариями к объявлениям.
 * <p>
 * Обеспечивает бизнес‑логику по:
 * - получению списка комментариев к объявлению;
 * - созданию, обновлению и удалению комментариев;
 * - проверке прав на редактирование/удаление (принадлежность комментария другому автору).
 * </p>
 */
@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final AdvertisingRepository advertisingRepository;
    private final SecurityHelper securityHelper;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param advertisingRepository репозиторий для работы с объявлениями
     * @param commentMapper         маппер для преобразования сущностей Comment в DTO
     * @param commentRepository     репозиторий для работы с комментариями
     * @param securityHelper        вспомогательный класс для получения данных текущего пользователя
     */
    public CommentService(AdvertisingRepository advertisingRepository,
                          CommentMapper commentMapper,
                          CommentRepository commentRepository,
                          SecurityHelper securityHelper) {
        this.commentMapper = commentMapper;
        this.commentRepository = commentRepository;
        this.advertisingRepository = advertisingRepository;
        this.securityHelper = securityHelper;
    }

    /**
     * Получает все комментарии к объявлению с подсчётом количества.
     * <p>
     * Сначала проверяет существование объявления (чтобы не возвращать пустой список
     * при несуществующем ID). Затем загружает комментарии вместе с авторами
     * (благодаря EntityGraph в репозитории) и преобразует их в DTO.
     * </p>
     *
     * @param id идентификатор объявления
     * @return DTO со списком комментариев и их количеством
     * @throws AdvertisingNotFoundException если объявление не найдено
     */
    public CommentsAllResponseDto findByAdvertisingId(Long id) {
        advertisingRepository.findById(id)
                .orElseThrow(() -> new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(id)));

        List<CommentOneResponseDto> commentListDto = commentRepository
                .findAllByAdvertisingId(id)
                .stream()
                .map(commentMapper::toResponse)
                .toList();

        return new CommentsAllResponseDto(commentListDto.size(), commentListDto);
    }

    /**
     * Добавляет новый комментарий к объявлению от имени текущего пользователя.
     * <p>
     * Проверяет существование объявления, создаёт сущность Comment, заполняет поля:
     * автор (переданный пользователь), текст, объявление, дата создания.
     * Сохраняет в БД и возвращает DTO созданного комментария.
     * </p>
     *
     * @param user      пользователь — автор комментария (должен быть авторизован)
     * @param id        идентификатор объявления
     * @param dto       DTO с текстом комментария
     * @return DTO созданного комментария
     * @throws AdvertisingNotFoundException если объявление не найдено
     */
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

    /**
     * Удаляет комментарий, если он принадлежит указанному объявлению.
     * <p>
     * Находит комментарий по паре (commentId, advertisingId), чтобы убедиться
     * в его принадлежности объявлению. Если комментарий не найден — выбрасывается
     * исключение. Иначе выполняется удаление по ID.
     * </p>
     *
     * @param commentId     идентификатор комментария
     * @param advertisingId идентификатор объявления
     * @throws CommentNotFoundException если комментарий не найден или не принадлежит объявлению
     */
    public void deleteCommentByIdAndAdvertisingById(Long commentId, Long advertisingId) {
        Long id = commentRepository.findByIdAndAdvertisingId(commentId, advertisingId)
                .orElseThrow(() -> new CommentNotFoundException(
                        ExceptionMessages.formatCommentNotFound(commentId)))
                .getId();
        commentRepository.deleteById(id);
    }

    /**
     * Обновляет текст комментария, если он принадлежит указанному объявлению.
     * <p>
     * Выполняется в транзакции, чтобы гарантировать целостность операции.
     * Находит комментарий по паре (commentId, advertisingId), обновляет поле text
     * и сохраняет изменения. Возвращает DTO обновлённого комментария.
     * </p>
     *
     * @param commentId     идентификатор комментария
     * @param advertisingId идентификатор объявления
     * @param dto           DTO с новым текстом комментария
     * @return DTO обновлённого комментария
     * @throws CommentNotFoundException если комментарий не найден или не принадлежит объявлению
     */
    @Transactional
    public CommentOneResponseDto updateCommentByIdAndAdvertisingById(
            Long commentId,
            Long advertisingId,
            CommentRequestDto dto) {

        Comment comment = commentRepository.findByIdAndAdvertisingId(
                        commentId,
                        advertisingId
                )
                .orElseThrow(() -> new CommentNotFoundException(
                        ExceptionMessages.formatCommentNotFound(commentId)
                ));

        comment.setText(dto.text());
        commentRepository.save(comment);
        return commentMapper.toResponse(comment);
    }

    /**
     * Проверяет, принадлежит ли комментарий другому автору (не текущему пользователю).
     * <p>
     * Используется для контроля прав: если комментарий принадлежит другому пользователю,
     * то текущий пользователь не должен иметь права на его редактирование/удаление.
     * Операция выполняется в транзакции (хотя по сути это read‑only проверка),
     * чтобы гарантировать согласованность данных в рамках бизнес‑операции.
     * </p>
     *
     * @param commentId     идентификатор комментария
     * @param advertisingId идентификатор объявления
     * @return true, если комментарий принадлежит другому автору; false — если автору текущего пользователя
     * @throws CommentNotFoundException если комментарий не найден или не принадлежит объявлению
     */
    @Transactional
    public boolean isAnotherAuthor(Long commentId, Long advertisingId) {
        Comment comment = commentRepository.findByIdAndAdvertisingId(
                        commentId,
                        advertisingId
                )
                .orElseThrow(() -> new CommentNotFoundException(
                        ExceptionMessages.formatCommentNotFound(commentId)
                ));

        Long currentUserId = securityHelper.getCurrentUserId();
        if (currentUserId == null) {
            // Если пользователь не авторизован, считаем, что это «другой» автор
            return true;
        }

        return !comment.getAuthor().getId().equals(currentUserId);
    }
}
