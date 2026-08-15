package ru.skypro.homework.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    // ===== КОНСТАНТЫ =====
    private static final Long COMMENT_ID = 1L;
    private static final Long NON_EXISTENT_COMMENT_ID = 999L;
    private static final Long AD_ID = 1L;
    private static final Long NON_EXISTENT_AD_ID = 999L;
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 999L;
    private static final String USER_EMAIL = "user@mail.com";
    private static final String USER_FIRST_NAME = "John";
    private static final String USER_LAST_NAME = "Doe";
    private static final String USER_PHONE = "+79991234567";
    private static final String USER_AVATAR_IMAGE = "/images/avatars/user.jpg";
    private static final String COMMENT_TEXT = "Great ad!";
    private static final String UPDATED_COMMENT_TEXT = "Updated comment text!";

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AdvertisingRepository advertisingRepository;

    @Mock
    private SecurityHelper securityHelper;

    @InjectMocks
    private CommentService commentService;

    // ===== МЕТОДЫ-ФАБРИКИ =====

    private User createDefaultUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        user.setPhone(USER_PHONE);
        user.setAvatarFileId(USER_AVATAR_IMAGE);
        return user;
    }

    private User createOtherUser() {
        User user = new User();
        user.setId(OTHER_USER_ID);
        user.setEmail("other@mail.com");
        user.setFirstName("Other");
        user.setLastName("User");
        user.setPhone("+79998887766");
        user.setAvatarFileId("/images/avatars/other.jpg");
        return user;
    }

    private Advertising createDefaultAdvertising() {
        Advertising ad = new Advertising();
        ad.setId(AD_ID);
        ad.setTitle("Test Ad");
        ad.setDescription("Test Description");
        ad.setPrice(100);
        ad.setAuthor(createDefaultUser());
        return ad;
    }

    private Comment createDefaultComment() {
        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        comment.setText(COMMENT_TEXT);
        comment.setAuthor(createDefaultUser());
        comment.setAdvertising(createDefaultAdvertising());
        comment.setCreatedAt(LocalDateTime.now());
        return comment;
    }

    private CommentRequestDto createDefaultCommentRequestDto() {
        return new CommentRequestDto(COMMENT_TEXT);
    }

    private CommentRequestDto createUpdatedCommentRequestDto() {
        return new CommentRequestDto(UPDATED_COMMENT_TEXT);
    }

    private CommentOneResponseDto createDefaultCommentOneResponseDto() {
        long createdAt = System.currentTimeMillis();
        return new CommentOneResponseDto(
                COMMENT_ID,
                USER_ID,
                USER_AVATAR_IMAGE,
                USER_FIRST_NAME,
                createdAt,
                COMMENT_TEXT
        );
    }

    private CommentOneResponseDto createUpdatedCommentOneResponseDto() {
        long createdAt = System.currentTimeMillis();
        return new CommentOneResponseDto(
                COMMENT_ID,
                USER_ID,
                USER_AVATAR_IMAGE,
                USER_FIRST_NAME,
                createdAt,
                UPDATED_COMMENT_TEXT
        );
    }

    private CommentOneResponseDto createSecondCommentOneResponseDto() {
        long createdAt = System.currentTimeMillis();
        return new CommentOneResponseDto(
                2L,
                OTHER_USER_ID,
                "/images/avatars/other.jpg",
                "Jane",
                createdAt,
                "Second comment"
        );
    }

    // ===== ТЕСТЫ ДЛЯ findByAdvertisingId =====

    @Test
    void findByAdvertisingId_Success_Test() {
        // Подготовка
        Long adId = AD_ID;
        Advertising ad = createDefaultAdvertising();
        Comment comment1 = createDefaultComment();
        Comment comment2 = createDefaultComment();
        comment2.setId(2L);
        comment2.setText("Second comment");

        List<Comment> comments = Arrays.asList(comment1, comment2);

        CommentOneResponseDto dto1 = createDefaultCommentOneResponseDto();
        CommentOneResponseDto dto2 = createSecondCommentOneResponseDto();

        when(advertisingRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(commentRepository.findAllByAdvertisingId(adId)).thenReturn(comments);
        when(commentMapper.toResponse(comment1)).thenReturn(dto1);
        when(commentMapper.toResponse(comment2)).thenReturn(dto2);

        // Действие
        CommentsAllResponseDto result = commentService.findByAdvertisingId(adId);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(2);
        assertThat(result.results()).hasSize(2);
        assertThat(result.results().get(0).pk()).isEqualTo(1L);
        assertThat(result.results().get(0).text()).isEqualTo(COMMENT_TEXT);
        assertThat(result.results().get(0).author()).isEqualTo(USER_ID);
        assertThat(result.results().get(0).authorFirstName()).isEqualTo(USER_FIRST_NAME);
        assertThat(result.results().get(1).pk()).isEqualTo(2L);

        verify(advertisingRepository, times(1)).findById(adId);
        verify(commentRepository, times(1)).findAllByAdvertisingId(adId);
        verify(commentMapper, times(2)).toResponse(any(Comment.class));
    }

    @Test
    void findByAdvertisingId_EmptyList_Test() {
        // Подготовка
        Long adId = AD_ID;
        Advertising ad = createDefaultAdvertising();

        when(advertisingRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(commentRepository.findAllByAdvertisingId(adId)).thenReturn(List.of());

        // Действие
        CommentsAllResponseDto result = commentService.findByAdvertisingId(adId);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(0);
        assertThat(result.results()).isEmpty();

        verify(advertisingRepository, times(1)).findById(adId);
        verify(commentRepository, times(1)).findAllByAdvertisingId(adId);
        verify(commentMapper, never()).toResponse(any());
    }

    @Test
    void findByAdvertisingId_AdNotFound_Test() {
        // Подготовка
        Long adId = NON_EXISTENT_AD_ID;

        // Мокаем оба вызова - сначала комментарии (пустой список), потом проверка объявления
        when(commentRepository.findAllByAdvertisingId(adId)).thenReturn(List.of());
        when(advertisingRepository.findById(adId)).thenReturn(Optional.empty());

        // Действие и проверка
        assertThatThrownBy(() -> commentService.findByAdvertisingId(adId))
                .isInstanceOf(AdvertisingNotFoundException.class)
                .hasMessage(ExceptionMessages.formatAdNotFound(adId));

        verify(commentRepository, times(1)).findAllByAdvertisingId(adId);
        verify(advertisingRepository, times(1)).findById(adId);
        verify(commentMapper, never()).toResponse(any());
    }

    // ===== ТЕСТЫ ДЛЯ addCommentToAdvertisingId =====

    @Test
    void addCommentToAdvertisingId_Success_Test() {
        // Подготовка
        User user = createDefaultUser();
        Long adId = AD_ID;
        CommentRequestDto dto = createDefaultCommentRequestDto();
        Advertising ad = createDefaultAdvertising();
        Comment comment = createDefaultComment();
        CommentOneResponseDto expected = createDefaultCommentOneResponseDto();

        when(advertisingRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(expected);

        // Действие
        CommentOneResponseDto result = commentService.addCommentToAdvertisingId(user, adId, dto);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result.pk()).isEqualTo(COMMENT_ID);
        assertThat(result.text()).isEqualTo(COMMENT_TEXT);
        assertThat(result.author()).isEqualTo(USER_ID);
        assertThat(result.authorFirstName()).isEqualTo(USER_FIRST_NAME);
        assertThat(result.authorImage()).isEqualTo(USER_AVATAR_IMAGE);

        verify(advertisingRepository, times(1)).findById(adId);
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(commentMapper, times(1)).toResponse(comment);
    }

    @Test
    void addCommentToAdvertisingId_AdNotFound_Test() {
        // Подготовка
        User user = createDefaultUser();
        Long adId = NON_EXISTENT_AD_ID;
        CommentRequestDto dto = createDefaultCommentRequestDto();

        when(advertisingRepository.findById(adId)).thenReturn(Optional.empty());

        // Действие и проверка
        assertThatThrownBy(() -> commentService.addCommentToAdvertisingId(user, adId, dto))
                .isInstanceOf(AdvertisingNotFoundException.class)
                .hasMessage(ExceptionMessages.formatAdNotFound(adId));

        verify(advertisingRepository, times(1)).findById(adId);
        verify(commentRepository, never()).save(any());
        verify(commentMapper, never()).toResponse(any());
    }

    @Test
    void addCommentToAdvertisingId_WithEmptyText_Test() {
        // Подготовка
        User user = createDefaultUser();
        Long adId = AD_ID;
        CommentRequestDto dto = new CommentRequestDto("");
        Advertising ad = createDefaultAdvertising();
        Comment comment = createDefaultComment();
        comment.setText("");
        long createdAt = System.currentTimeMillis();
        CommentOneResponseDto expected = new CommentOneResponseDto(
                COMMENT_ID, USER_ID, USER_AVATAR_IMAGE, USER_FIRST_NAME, createdAt, ""
        );

        when(advertisingRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(expected);

        // Действие
        CommentOneResponseDto result = commentService.addCommentToAdvertisingId(user, adId, dto);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result.text()).isEmpty();

        verify(advertisingRepository, times(1)).findById(adId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    // ===== ТЕСТЫ ДЛЯ deleteCommentByIdAndAdvertisingById =====

    @Test
    void deleteCommentByIdAndAdvertisingById_Success_Test() {
        // Подготовка
        Long commentId = COMMENT_ID;
        Long adId = AD_ID;
        Comment comment = createDefaultComment();

        when(commentRepository.findByIdAndAdvertisingId(commentId, adId))
                .thenReturn(Optional.of(comment));
        doNothing().when(commentRepository).deleteById(commentId);

        // Действие
        commentService.deleteCommentByIdAndAdvertisingById(commentId, adId);

        // Проверка
        verify(commentRepository, times(1)).findByIdAndAdvertisingId(commentId, adId);
        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    void deleteCommentByIdAndAdvertisingById_CommentNotFound_Test() {
        // Подготовка
        Long commentId = NON_EXISTENT_COMMENT_ID;
        Long adId = AD_ID;

        when(commentRepository.findByIdAndAdvertisingId(commentId, adId))
                .thenReturn(Optional.empty());

        // Действие и проверка
        assertThatThrownBy(() -> commentService.deleteCommentByIdAndAdvertisingById(commentId, adId))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage(ExceptionMessages.formatCommentNotFound(commentId));

        verify(commentRepository, times(1)).findByIdAndAdvertisingId(commentId, adId);
        verify(commentRepository, never()).deleteById(anyLong());
    }

    // ===== ТЕСТЫ ДЛЯ updateCommentByIdAndAdvertisingById =====

    @Test
    void updateCommentByIdAndAdvertisingById_Success_Test() {
        // Подготовка
        Long commentId = COMMENT_ID;
        Long adId = AD_ID;
        CommentRequestDto dto = createUpdatedCommentRequestDto();
        Comment comment = createDefaultComment();
        CommentOneResponseDto expected = createUpdatedCommentOneResponseDto();

        when(commentRepository.findByIdAndAdvertisingId(commentId, adId))
                .thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(expected);

        // Действие
        CommentOneResponseDto result = commentService.updateCommentByIdAndAdvertisingById(commentId, adId, dto);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result.pk()).isEqualTo(COMMENT_ID);
        assertThat(result.text()).isEqualTo(UPDATED_COMMENT_TEXT);
        assertThat(result.author()).isEqualTo(USER_ID);

        verify(commentRepository, times(1)).findByIdAndAdvertisingId(commentId, adId);
        verify(commentRepository, times(1)).save(comment);
        verify(commentMapper, times(1)).toResponse(comment);
        assertThat(comment.getText()).isEqualTo(UPDATED_COMMENT_TEXT);
    }

    @Test
    void updateCommentByIdAndAdvertisingById_CommentNotFound_Test() {
        // Подготовка
        Long commentId = NON_EXISTENT_COMMENT_ID;
        Long adId = AD_ID;
        CommentRequestDto dto = createUpdatedCommentRequestDto();

        when(commentRepository.findByIdAndAdvertisingId(commentId, adId))
                .thenReturn(Optional.empty());

        // Действие и проверка
        assertThatThrownBy(() -> commentService.updateCommentByIdAndAdvertisingById(commentId, adId, dto))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage(ExceptionMessages.formatCommentNotFound(commentId));

        verify(commentRepository, times(1)).findByIdAndAdvertisingId(commentId, adId);
        verify(commentRepository, never()).save(any());
        verify(commentMapper, never()).toResponse(any());
    }

    @Test
    void updateCommentByIdAndAdvertisingById_WithEmptyText_Test() {
        // Подготовка
        Long commentId = COMMENT_ID;
        Long adId = AD_ID;
        CommentRequestDto dto = new CommentRequestDto("");
        Comment comment = createDefaultComment();
        long createdAt = System.currentTimeMillis();
        CommentOneResponseDto expected = new CommentOneResponseDto(
                COMMENT_ID, USER_ID, USER_AVATAR_IMAGE, USER_FIRST_NAME, createdAt, ""
        );

        when(commentRepository.findByIdAndAdvertisingId(commentId, adId))
                .thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(expected);

        // Действие
        CommentOneResponseDto result = commentService.updateCommentByIdAndAdvertisingById(commentId, adId, dto);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result.text()).isEmpty();

        verify(commentRepository, times(1)).findByIdAndAdvertisingId(commentId, adId);
        verify(commentRepository, times(1)).save(comment);
        assertThat(comment.getText()).isEmpty();
    }

    // ===== ТЕСТЫ ДЛЯ isAnotherAuthor =====

    @Test
    void isAnotherAuthor_WhenUserIsNotAuthor_Test() {
        // Подготовка
        Long commentId = COMMENT_ID;
        Long adId = AD_ID;
        Comment comment = createDefaultComment();

        when(commentRepository.findByIdAndAdvertisingId(commentId, adId))
                .thenReturn(Optional.of(comment));
        when(securityHelper.getCurrentUserId()).thenReturn(OTHER_USER_ID);

        // Действие
        boolean result = commentService.isAnotherAuthor(commentId, adId);

        // Проверка
        assertThat(result).isTrue();
        verify(commentRepository, times(1)).findByIdAndAdvertisingId(commentId, adId);
        verify(securityHelper, times(1)).getCurrentUserId();
    }

    @Test
    void isAnotherAuthor_WhenUserIsAuthor_Test() {
        // Подготовка
        Long commentId = COMMENT_ID;
        Long adId = AD_ID;
        Comment comment = createDefaultComment();

        when(commentRepository.findByIdAndAdvertisingId(commentId, adId))
                .thenReturn(Optional.of(comment));
        when(securityHelper.getCurrentUserId()).thenReturn(USER_ID);

        // Действие
        boolean result = commentService.isAnotherAuthor(commentId, adId);

        // Проверка
        assertThat(result).isFalse();
        verify(commentRepository, times(1)).findByIdAndAdvertisingId(commentId, adId);
        verify(securityHelper, times(1)).getCurrentUserId();
    }

    @Test
    void isAnotherAuthor_CommentNotFound_Test() {
        // Подготовка
        Long commentId = NON_EXISTENT_COMMENT_ID;
        Long adId = AD_ID;

        when(commentRepository.findByIdAndAdvertisingId(commentId, adId))
                .thenReturn(Optional.empty());

        // Действие и проверка
        assertThatThrownBy(() -> commentService.isAnotherAuthor(commentId, adId))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage(ExceptionMessages.formatCommentNotFound(commentId));

        verify(commentRepository, times(1)).findByIdAndAdvertisingId(commentId, adId);
        verify(securityHelper, never()).getCurrentUserId();
    }
}