package ru.skypro.homework.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.homework.constants.ExceptionMessages;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.exception.AdvertisingNotFoundException;
import ru.skypro.homework.model.User;
import ru.skypro.homework.security.SecurityHelper;
import ru.skypro.homework.service.AdvertisingService;
import ru.skypro.homework.service.CommentService;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdsController.class)
public class AdsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdvertisingService adsService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private SecurityHelper securityHelper;

    @Autowired
    private ObjectMapper objectMapper;

    // ===== ПРИВАТНЫЕ КОНСТАНТЫ =====

    private static final Long AD_ID = 1L;
    private static final Long NON_EXISTENT_AD_ID = -9L;
    private static final Long AUTHOR_ID = 100L;
    private static final String TITLE = "iPhone 15 Pro Max";
    private static final String DESCRIPTION = "Описание объявления";
    private static final Integer PRICE = 499;
    private static final String IMAGE_PATH = "/images/ad_1.jpg";

    private static final String USERNAME = "user";
    private static final String USER_EMAIL = "youmail@google.com";
    private static final String USER_FIRST_NAME = "authorFirstName";
    private static final String USER_LAST_NAME = "authorLastName";
    private static final String USER_PHONE = "+7499 123123";
    private static final String USER_IMAGE = "picture.jpg";

    private static final String AD_TITLE = "Заголовок объявления";
    private static final String AD_DESCRIPTION = "Описание объявления";
    private static final Integer AD_PRICE = 123;

    private static final Long DELETE_AD_ID = 1L;

    // ===== МЕТОД-ПОМОЩНИК ДЛЯ СОЗДАНИЯ AUTHORITIES =====

    private Collection<GrantedAuthority> createAuthorities(String... roles) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

    // ===== ПРИВАТНЫЕ МЕТОДЫ ДЛЯ СОЗДАНИЯ ТЕСТОВЫХ ДАННЫХ =====

    private CreateOrUpdateAd createDefaultProperties() {
        return new CreateOrUpdateAd(AD_PRICE, AD_TITLE, AD_DESCRIPTION);
    }

    private MockMultipartFile createDefaultImage() {
        return new MockMultipartFile(
                "image",
                "ad_1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );
    }

    private MockMultipartFile createPropertiesPart(CreateOrUpdateAd properties) throws Exception {
        String propertiesJson = objectMapper.writeValueAsString(properties);
        return new MockMultipartFile(
                "properties",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                propertiesJson.getBytes()
        );
    }

    private AdvertisingWithAuthorDto createDefaultAdvertisingWithAuthorDto() {
        return new AdvertisingWithAuthorDto(
                AD_ID,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                DESCRIPTION,
                USER_EMAIL,
                USER_IMAGE,
                USER_PHONE,
                "123",
                AD_TITLE
        );
    }

    private AdvertisingOneResponseDto createDefaultAdvertisingOneResponseDto() {
        return new AdvertisingOneResponseDto(
                AD_ID,
                AUTHOR_ID,
                IMAGE_PATH,
                PRICE,
                TITLE
        );
    }

    // ===== НАСТРОЙКА ДЛЯ КАЖДОГО ТЕСТА =====

    @BeforeEach
    void setUp() {
    }

    // ===== ТЕСТЫ =====

    @Test
    void getAdsById_Success_Test() throws Exception {
        AdvertisingWithAuthorDto expected = createDefaultAdvertisingWithAuthorDto();

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(adsService.getAdById(AD_ID)).thenReturn(expected);

        mockMvc.perform(get("/ads/" + AD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(AD_ID))
                .andExpect(jsonPath("$.authorFirstName").value(USER_FIRST_NAME))
                .andExpect(jsonPath("$.description").value(DESCRIPTION))
                .andExpect(jsonPath("$.title").value(AD_TITLE));
    }

    @Test
    void getAdsById_NotFound_Test() throws Exception {
        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(adsService.getAdById(NON_EXISTENT_AD_ID))
                .thenThrow(new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(NON_EXISTENT_AD_ID)));

        mockMvc.perform(get("/ads/" + NON_EXISTENT_AD_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAdsById_Unauthorized_Test() throws Exception {
        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(get("/ads/" + AD_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteAdsById_Success_Test() throws Exception {
        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(true);
        when(adsService.isAnotherAuthor(DELETE_AD_ID)).thenReturn(false);

        doNothing().when(adsService).deleteAdById(DELETE_AD_ID);

        mockMvc.perform(delete("/ads/" + DELETE_AD_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAdsById_Forbidden_When_IsNotAdmin_And_isAnotherAuthor_Test() throws Exception {
        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(false);
        when(adsService.isAnotherAuthor(DELETE_AD_ID)).thenReturn(true);

        mockMvc.perform(delete("/ads/" + DELETE_AD_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteAdsById_When_IsAdmin_And_IsAnotherAuthor_Test() throws Exception {
        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(true);
        when(adsService.isAnotherAuthor(DELETE_AD_ID)).thenReturn(true);

        doNothing().when(adsService).deleteAdById(DELETE_AD_ID);

        mockMvc.perform(delete("/ads/" + DELETE_AD_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAdsById_Unauthorized_Test() throws Exception {
        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(delete("/ads/" + DELETE_AD_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addAds_Success_Test() throws Exception {
        CreateOrUpdateAd properties = createDefaultProperties();
        MockMultipartFile image = createDefaultImage();
        MockMultipartFile propertiesPart = createPropertiesPart(properties);

        AdvertisingOneResponseDto adsSavedDto = createDefaultAdvertisingOneResponseDto();

        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);
        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(adsService.createAds(securityHelper.getCurrentUsername(), properties, image))
                .thenReturn(adsSavedDto);

        mockMvc.perform(multipart("/ads")
                        .file(image)
                        .file(propertiesPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pk").value(AD_ID))
                .andExpect(jsonPath("$.author").value(AUTHOR_ID))
                .andExpect(jsonPath("$.image").value(IMAGE_PATH))
                .andExpect(jsonPath("$.price").value(PRICE))
                .andExpect(jsonPath("$.title").value(TITLE));
    }

    @Test
    void addAds_Unauthorized_Test() throws Exception {
        CreateOrUpdateAd properties = createDefaultProperties();
        MockMultipartFile image = createDefaultImage();
        MockMultipartFile propertiesPart = createPropertiesPart(properties);

        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);
        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(multipart("/ads")
                        .file(image)
                        .file(propertiesPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    // ===== ТЕСТЫ ДЛЯ updateAdsImage (PATCH /ads/{id}/image) =====

    @Test
    void updateAdsImage_Success_Test() throws Exception {
        Long adId = 1L;
        String fileName = "new_image.jpg";
        byte[] fileContent = "fake image content".getBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                fileName,
                MediaType.IMAGE_JPEG_VALUE,
                fileContent
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        doReturn(createAuthorities("ROLE_USER")).when(securityHelper).getAuthorities();
        when(securityHelper.isAdmin()).thenReturn(false);
        when(adsService.isAnotherAuthor(adId)).thenReturn(false);
        doNothing().when(adsService).updateAdsImage(adId, file);

        mockMvc.perform(multipart("/ads/{id}/image", adId)
                        .file(file)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().string(fileName));
    }

    @Test
    void updateAdsImage_Unauthorized_Test() throws Exception {
        Long adId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(multipart("/ads/{id}/image", adId)
                        .file(file)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isUnauthorized());

        verify(adsService, never()).updateAdsImage(anyLong(), any());
    }

    @Test
    void updateAdsImage_Forbidden_WhenNoRole_Test() throws Exception {
        Long adId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        doReturn(createAuthorities()).when(securityHelper).getAuthorities();

        mockMvc.perform(multipart("/ads/{id}/image", adId)
                        .file(file)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isForbidden());

        verify(adsService, never()).updateAdsImage(anyLong(), any());
    }

    @Test
    void updateAdsImage_Forbidden_WhenAnotherAuthorAndNotAdmin_Test() throws Exception {
        Long adId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        doReturn(createAuthorities("ROLE_USER")).when(securityHelper).getAuthorities();
        when(securityHelper.isAdmin()).thenReturn(false);
        when(adsService.isAnotherAuthor(adId)).thenReturn(true);

        mockMvc.perform(multipart("/ads/{id}/image", adId)
                        .file(file)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isForbidden());

        verify(adsService, never()).updateAdsImage(anyLong(), any());
    }

    @Test
    void updateAdsImage_Success_WhenAdminUpdatesAnotherAuthor_Test() throws Exception {
        Long adId = 1L;
        String fileName = "admin_updated.jpg";
        byte[] fileContent = "admin content".getBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                fileName,
                MediaType.IMAGE_JPEG_VALUE,
                fileContent
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        doReturn(createAuthorities("ROLE_ADMIN")).when(securityHelper).getAuthorities();
        when(securityHelper.isAdmin()).thenReturn(true);
        when(adsService.isAnotherAuthor(adId)).thenReturn(true);
        doNothing().when(adsService).updateAdsImage(adId, file);

        mockMvc.perform(multipart("/ads/{id}/image", adId)
                        .file(file)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().string(fileName));

        verify(adsService, times(1)).updateAdsImage(adId, file);
    }

    // ===== ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ =====
    @Test
    void getAllAds_Success_Test() throws Exception {
        // Создаем тестовые данные
        AdvertisingAllResponseDto expected = new AdvertisingAllResponseDto(
                2,  // count
                List.of(
                        new AdvertisingOneResponseDto(1L, 100L, "/img1.jpg", 100, "Ad1"),
                        new AdvertisingOneResponseDto(2L, 100L, "/img2.jpg", 200, "Ad2")
                )
        );

        when(adsService.findAll()).thenReturn(expected);

        mockMvc.perform(get("/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.results[0].pk").value(1L))
                .andExpect(jsonPath("$.results[0].title").value("Ad1"));
    }

    @Test
    void getAdsAuthorisedUser_Success_Test() throws Exception {
        // Создаем тестовые данные
        AdvertisingAllResponseDto expected = new AdvertisingAllResponseDto(
                1,  // count
                List.of(
                        new AdvertisingOneResponseDto(1L, 100L, "/img1.jpg", 100, "My Ad")
                )
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.getCurrentUserId()).thenReturn(1L);
        when(adsService.findAllByUserId(1L)).thenReturn(expected);

        mockMvc.perform(get("/ads/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.results[0].title").value("My Ad"));
    }

    @Test
    void getAdsAuthorisedUser_Unauthorized_Test() throws Exception {
        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(get("/ads/me"))
                .andExpect(status().isUnauthorized());
    }

    // ===== ТЕСТЫ ДЛЯ КОММЕНТАРИЕВ =====

    @Test
    void getAllCommentsByAdsId_Success_Test() throws Exception {
        Long adId = 1L;
        CommentsAllResponseDto expected = new CommentsAllResponseDto(
                2,
                List.of(
                        new CommentOneResponseDto(1L, 1L, "user1@mail.com", "Great ad!", 1234567890L, "Great ad!"),
                        new CommentOneResponseDto(2L, 2L, "user2@mail.com", "Nice product!", 1234567891L, "Nice product!")
                )
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(commentService.findByAdvertisingId(adId)).thenReturn(expected);

        mockMvc.perform(get("/ads/{id}/comments", adId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.results[0].pk").value(1L))
                .andExpect(jsonPath("$.results[0].text").value("Great ad!"))
                .andExpect(jsonPath("$.results[1].pk").value(2L))
                .andExpect(jsonPath("$.results[1].text").value("Nice product!"));
    }

    @Test
    void getAllCommentsByAdsId_Unauthorized_Test() throws Exception {
        Long adId = 1L;

        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(get("/ads/{id}/comments", adId))
                .andExpect(status().isUnauthorized());

        verify(commentService, never()).findByAdvertisingId(anyLong());
    }

    @Test
    void getAllCommentsByAdsId_AdNotFound_Test() throws Exception {
        Long adId = 999L;

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(commentService.findByAdvertisingId(adId))
                .thenThrow(new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(adId)));

        mockMvc.perform(get("/ads/{id}/comments", adId))
                .andExpect(status().isNotFound());
    }

    @Test
    void addCommentToAdvertisingId_Success_Test() throws Exception {
        Long adId = 1L;
        String text = "Комментарий";
        CommentRequestDto commentRequest = new CommentRequestDto(text);
        String commentJson = objectMapper.writeValueAsString(commentRequest);

        MockMultipartFile commentPart = new MockMultipartFile(
                "text",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                commentJson.getBytes()
        );

        CommentOneResponseDto expected = new CommentOneResponseDto(
                1L,
                1L,
                "user@mail.com",
                text,
                1234567890L,
                "Комментарий"
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.getCurrentUser()).thenReturn(new User(1L, "user@mail.com", "password", "FirstName", "LastName", "+79991234567", Role.USER, "image.jpg"));
        when(commentService.addCommentToAdvertisingId(any(), eq(adId), any(CommentRequestDto.class)))
                .thenReturn(expected);

        mockMvc.perform(multipart("/ads/{id}/comments", adId)
                        .file(commentPart)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(1L))
                .andExpect(jsonPath("$.text").value(text))
                .andExpect(jsonPath("$.author").value(1L));
    }

    @Test
    void addCommentToAdvertisingId_Unauthorized_Test() throws Exception {
        Long adId = 1L;
        String text = "This is a test comment";
        CommentRequestDto commentRequest = new CommentRequestDto(text);
        String commentJson = objectMapper.writeValueAsString(commentRequest);

        MockMultipartFile commentPart = new MockMultipartFile(
                "text",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                commentJson.getBytes()
        );

        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(multipart("/ads/{id}/comments", adId)
                        .file(commentPart)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        }))
                .andExpect(status().isUnauthorized());

        verify(commentService, never()).addCommentToAdvertisingId(any(), anyLong(), any());
    }

    @Test
    void deleteComment_Success_Test() throws Exception {
        Long adId = 1L;
        Long commentId = 1L;

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(true);
        when(commentService.isAnotherAuthor(commentId, adId)).thenReturn(false);
        doNothing().when(commentService).deleteCommentByIdAndAdvertisingById(commentId, adId);

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", adId, commentId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteComment_Unauthorized_Test() throws Exception {
        Long adId = 1L;
        Long commentId = 1L;

        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", adId, commentId))
                .andExpect(status().isUnauthorized());

        verify(commentService, never()).deleteCommentByIdAndAdvertisingById(anyLong(), anyLong());
    }

    @Test
    void deleteComment_Forbidden_WhenAnotherAuthorAndNotAdmin_Test() throws Exception {
        Long adId = 1L;
        Long commentId = 1L;

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(false);
        when(commentService.isAnotherAuthor(commentId, adId)).thenReturn(true);

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", adId, commentId))
                .andExpect(status().isForbidden());

        verify(commentService, never()).deleteCommentByIdAndAdvertisingById(anyLong(), anyLong());
    }

    @Test
    void deleteComment_Success_WhenAdminDeletesAnotherAuthor_Test() throws Exception {
        Long adId = 1L;
        Long commentId = 1L;

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(true);
        when(commentService.isAnotherAuthor(commentId, adId)).thenReturn(true);
        doNothing().when(commentService).deleteCommentByIdAndAdvertisingById(commentId, adId);

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", adId, commentId))
                .andExpect(status().isOk());

        verify(commentService, times(1)).deleteCommentByIdAndAdvertisingById(commentId, adId);
    }

    @Test
    void updateComment_Success_Test() throws Exception {
        Long adId = 1L;
        Long commentId = 1L;
        String newText = "Updated comment text";
        String authorFirstname = "firstname";
        CommentRequestDto commentRequest = new CommentRequestDto(newText);
        String commentJson = objectMapper.writeValueAsString(commentRequest);

        MockMultipartFile commentPart = new MockMultipartFile(
                "text",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                commentJson.getBytes()
        );

        CommentOneResponseDto expected = new CommentOneResponseDto(
                commentId,
                1L,
                "user@mail.com",
                authorFirstname,
                1234567890L,
                newText
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(false);
        when(commentService.isAnotherAuthor(commentId, adId)).thenReturn(false);
        when(commentService.updateCommentByIdAndAdvertisingById(commentId, adId, commentRequest))
                .thenReturn(expected);

        mockMvc.perform(multipart("/ads/{adId}/comments/{commentId}", adId, commentId)
                        .file(commentPart)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(commentId))
                .andExpect(jsonPath("$.text").value(newText));
    }

    @Test
    void updateComment_Unauthorized_Test() throws Exception {
        Long adId = 1L;
        Long commentId = 1L;
        String newText = "Updated comment text";
        CommentRequestDto commentRequest = new CommentRequestDto(newText);
        String commentJson = objectMapper.writeValueAsString(commentRequest);

        MockMultipartFile commentPart = new MockMultipartFile(
                "text",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                commentJson.getBytes()
        );

        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(multipart("/ads/{adId}/comments/{commentId}", adId, commentId)
                        .file(commentPart)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isUnauthorized());

        verify(commentService, never()).updateCommentByIdAndAdvertisingById(anyLong(), anyLong(), any());
    }

    @Test
    void updateComment_Forbidden_WhenAnotherAuthorAndNotAdmin_Test() throws Exception {
        Long adId = 1L;
        Long commentId = 1L;
        String newText = "Updated comment text";
        CommentRequestDto commentRequest = new CommentRequestDto(newText);
        String commentJson = objectMapper.writeValueAsString(commentRequest);

        MockMultipartFile commentPart = new MockMultipartFile(
                "text",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                commentJson.getBytes()
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(false);
        when(commentService.isAnotherAuthor(commentId, adId)).thenReturn(true);

        mockMvc.perform(multipart("/ads/{adId}/comments/{commentId}", adId, commentId)
                        .file(commentPart)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isForbidden());

        verify(commentService, never()).updateCommentByIdAndAdvertisingById(anyLong(), anyLong(), any());
    }

    @Test
    void updateComment_Success_WhenAdminUpdatesAnotherAuthor_Test() throws Exception {
        Long adId = 1L;
        Long commentId = 1L;
        String newText = "Admin updated comment";
        String authorFirstname = "firstname";
        CommentRequestDto commentRequest = new CommentRequestDto(newText);
        String commentJson = objectMapper.writeValueAsString(commentRequest);

        MockMultipartFile commentPart = new MockMultipartFile(
                "text",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                commentJson.getBytes()
        );

        CommentOneResponseDto expected = new CommentOneResponseDto(
                commentId,
                2L,
                "other@mail.com",
                authorFirstname,
                1234567890L,
                newText
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(true);
        when(commentService.isAnotherAuthor(commentId, adId)).thenReturn(true);
        when(commentService.updateCommentByIdAndAdvertisingById(commentId, adId, commentRequest))
                .thenReturn(expected);

        mockMvc.perform(multipart("/ads/{adId}/comments/{commentId}", adId, commentId)
                        .file(commentPart)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(commentId))
                .andExpect(jsonPath("$.text").value(newText));
    }
}