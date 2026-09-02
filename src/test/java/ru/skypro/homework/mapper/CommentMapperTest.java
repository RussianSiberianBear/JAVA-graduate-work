package ru.skypro.homework.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.skypro.homework.dto.CommentOneResponseDto;
import ru.skypro.homework.model.Advertising;
import ru.skypro.homework.model.Comment;
import ru.skypro.homework.model.User;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CommentMapperTest {

    @Autowired
    private CommentMapper commentMapper;

    // ===== КОНСТАНТЫ =====
    private static final Long COMMENT_ID = 1L;
    private static final Long AUTHOR_ID = 1L;
    private static final Long AD_ID = 1L;
    private static final String COMMENT_TEXT = "Great ad!";
    private static final String AUTHOR_FIRST_NAME = "John";
    private static final String AUTHOR_EMAIL = "john@mail.com";
    private static final String AVATAR_FILE_ID = "avatar123";
    private static final String EXPECTED_AVATAR_URL = "/images/avatar123";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

    // ===== МЕТОДЫ-ФАБРИКИ =====

    private User createDefaultAuthor() {
        User user = new User();
        user.setId(AUTHOR_ID);
        user.setFirstName(AUTHOR_FIRST_NAME);
        user.setEmail(AUTHOR_EMAIL);
        user.setAvatarFileId(AVATAR_FILE_ID);
        return user;
    }

    private Advertising createDefaultAdvertising() {
        Advertising ad = new Advertising();
        ad.setId(AD_ID);
        ad.setTitle("Test Ad");
        return ad;
    }

    private Comment createDefaultComment() {
        Comment comment = new Comment();
        comment.setId(COMMENT_ID);
        comment.setText(COMMENT_TEXT);
        comment.setAuthor(createDefaultAuthor());
        comment.setAdvertising(createDefaultAdvertising());
        comment.setCreatedAt(CREATED_AT);
        return comment;
    }

    // ===== ТЕСТЫ =====

    @Test
    void toResponse_ShouldMapCommentToResponseDto() {
        // Подготовка
        Comment comment = createDefaultComment();

        // Действие
        CommentOneResponseDto result = commentMapper.toResponse(comment);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result.pk()).isEqualTo(COMMENT_ID);
        assertThat(result.text()).isEqualTo(COMMENT_TEXT);
        assertThat(result.author()).isEqualTo(AUTHOR_ID);
        assertThat(result.authorFirstName()).isEqualTo(AUTHOR_FIRST_NAME);
        assertThat(result.authorImage()).isEqualTo(EXPECTED_AVATAR_URL);
        assertThat(result.createdAt()).isEqualTo(
                CREATED_AT.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
    }

    @Test
    void toResponse_ShouldHandleNullAvatarFileId_Test() {
        // Подготовка
        Comment comment = createDefaultComment();
        comment.getAuthor().setAvatarFileId(null);

        // Действие
        CommentOneResponseDto result = commentMapper.toResponse(comment);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result.authorImage()).isNull();
    }

    @Test
    void toResponse_ShouldHandleNullCreatedAt_Test() {
        // Подготовка
        Comment comment = createDefaultComment();
        comment.setCreatedAt(null);

        // Действие
        CommentOneResponseDto result = commentMapper.toResponse(comment);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result.createdAt()).isNull();
    }

    @Test
    void toResponse_ShouldHandleNullAuthor_Test() {
        // Подготовка
        Comment comment = createDefaultComment();
        comment.setAuthor(null);

        // Действие
        CommentOneResponseDto result = commentMapper.toResponse(comment);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result.pk()).isEqualTo(COMMENT_ID);
        assertThat(result.text()).isEqualTo(COMMENT_TEXT);
        assertThat(result.author()).isNull();
        assertThat(result.authorFirstName()).isNull();
        assertThat(result.authorImage()).isNull();
    }

    @Test
    void toImageUrl_ShouldConvertFileIdToUrl() {
        String result = ImageMapperUtil.toImageUrl(AVATAR_FILE_ID);
        assertThat(result).isEqualTo(EXPECTED_AVATAR_URL);
    }

    @Test
    void toImageUrl_ShouldReturnNullForNullFileId() {
        String result = ImageMapperUtil.toImageUrl(null);
        assertThat(result).isNull();
    }

    @Test
    void toTimestamp_ShouldConvertLocalDateTimeToTimestamp() {
        Long result = commentMapper.toTimestamp(CREATED_AT);
        assertThat(result).isEqualTo(
                CREATED_AT.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
    }

    @Test
    void toTimestamp_ShouldReturnNullForNullDateTime() {
        Long result = commentMapper.toTimestamp(null);
        assertThat(result).isNull();
    }
}