package ru.skypro.homework.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdsControllerTest {

    private static final Long AD_ID = 1L;
    private static final Long NON_EXISTENT_AD_ID = 999L;
    private static final Long AUTHOR_ID = 100L;
    private static final Long COMMENT_ID = 10L;

    private static final String USERNAME = "user@mail.com";
    private static final String USER_FIRST_NAME = "Ivan";
    private static final String USER_LAST_NAME = "Ivanov";
    private static final String USER_EMAIL = "user@mail.com";
    private static final String USER_PHONE = "+79991234567";
    private static final String USER_IMAGE = "/images/avatar-1";

    private static final String AD_TITLE = "iPhone 15 Pro Max";
    private static final String AD_DESCRIPTION = "Описание объявления";
    private static final Integer AD_PRICE = 499;
    private static final String AD_IMAGE = "/images/ad-1";

    private static final String COMMENT_TEXT = "Хорошее объявление";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdvertisingService advertisingService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private SecurityHelper securityHelper;

    // ===== Helpers =====

    private CreateOrUpdateAd defaultAdRequest() {
        return new CreateOrUpdateAd(AD_PRICE, AD_TITLE, AD_DESCRIPTION);
    }

    private AdvertisingOneResponseDto defaultAdResponse() {
        return new AdvertisingOneResponseDto(AD_ID, AUTHOR_ID, AD_IMAGE, AD_PRICE, AD_TITLE);
    }

    private AdvertisingWithAuthorDto defaultExtendedAdResponse() {
        return new AdvertisingWithAuthorDto(
                AD_ID,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                AD_DESCRIPTION,
                USER_EMAIL,
                USER_IMAGE,
                USER_PHONE,
                String.valueOf(AD_PRICE),
                AD_TITLE
        );
    }

    private CommentOneResponseDto defaultCommentResponse() {
        return new CommentOneResponseDto(
                COMMENT_ID,
                AUTHOR_ID,
                USER_IMAGE,
                USER_FIRST_NAME,
                1_700_000_000_000L,
                COMMENT_TEXT
        );
    }

    private MockMultipartFile imagePart(String fileName) {
        return new MockMultipartFile(
                "image",
                fileName,
                MediaType.IMAGE_JPEG_VALUE,
                "image-bytes".getBytes()
        );
    }

    private MockMultipartFile propertiesPart(CreateOrUpdateAd request) throws Exception {
        return new MockMultipartFile(
                "properties",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }

    // ===== GET /ads =====

    @Test
    void getAllAds_Success_WithoutAuthentication_Test() throws Exception {
        AdvertisingAllResponseDto response = new AdvertisingAllResponseDto(
                2,
                List.of(
                        defaultAdResponse(),
                        new AdvertisingOneResponseDto(2L, AUTHOR_ID, "/images/ad-2", 700, "MacBook")
                )
        );
        when(advertisingService.findAll()).thenReturn(response);

        mockMvc.perform(get("/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.results[0].pk").value(AD_ID))
                .andExpect(jsonPath("$.results[0].title").value(AD_TITLE));

        verify(advertisingService).findAll();
    }

    // ===== GET /ads/{id} =====

    @Test
    void getAdsById_Success_Test() throws Exception {
        when(advertisingService.getAdById(AD_ID)).thenReturn(defaultExtendedAdResponse());

        mockMvc.perform(get("/ads/{id}", AD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(AD_ID))
                .andExpect(jsonPath("$.authorFirstName").value(USER_FIRST_NAME))
                .andExpect(jsonPath("$.description").value(AD_DESCRIPTION))
                .andExpect(jsonPath("$.title").value(AD_TITLE));
    }

    @Test
    void getAdsById_NotFound_Test() throws Exception {
        when(advertisingService.getAdById(NON_EXISTENT_AD_ID))
                .thenThrow(new AdvertisingNotFoundException(
                        ExceptionMessages.formatAdNotFound(NON_EXISTENT_AD_ID)));

        mockMvc.perform(get("/ads/{id}", NON_EXISTENT_AD_ID))
                .andExpect(status().isNotFound());
    }

    // ===== POST /ads =====

    @Test
    void addAds_Success_Test() throws Exception {
        CreateOrUpdateAd request = defaultAdRequest();
        MockMultipartFile image = imagePart("ad.jpg");

        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);
        when(advertisingService.createAds(USERNAME, request, image)).thenReturn(defaultAdResponse());

        mockMvc.perform(multipart("/ads")
                        .file(propertiesPart(request))
                        .file(image))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pk").value(AD_ID))
                .andExpect(jsonPath("$.author").value(AUTHOR_ID))
                .andExpect(jsonPath("$.image").value(AD_IMAGE))
                .andExpect(jsonPath("$.price").value(AD_PRICE))
                .andExpect(jsonPath("$.title").value(AD_TITLE));
    }

    // ===== DELETE /ads/{id} =====

    @Test
    void deleteAdsById_Success_WhenOwner_Test() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(false);
        when(advertisingService.isAnotherAuthor(AD_ID)).thenReturn(false);

        mockMvc.perform(delete("/ads/{id}", AD_ID))
                .andExpect(status().isNoContent());

        verify(advertisingService).deleteAdById(AD_ID);
    }

    @Test
    void deleteAdsById_Forbidden_WhenAnotherAuthorAndNotAdmin_Test() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(false);
        when(advertisingService.isAnotherAuthor(AD_ID)).thenReturn(true);

        mockMvc.perform(delete("/ads/{id}", AD_ID))
                .andExpect(status().isForbidden());

        verify(advertisingService, never()).deleteAdById(anyLong());
    }

    @Test
    void deleteAdsById_Success_WhenAdmin_Test() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(true);

        mockMvc.perform(delete("/ads/{id}", AD_ID))
                .andExpect(status().isNoContent());

        verify(advertisingService, never()).isAnotherAuthor(anyLong());
        verify(advertisingService).deleteAdById(AD_ID);
    }

    // ===== PATCH /ads/{id} =====

    @Test
    void updateAdsById_Success_WhenOwner_Test() throws Exception {
        CreateOrUpdateAd request = defaultAdRequest();
        when(securityHelper.isAdmin()).thenReturn(false);
        when(advertisingService.isAnotherAuthor(AD_ID)).thenReturn(false);
        when(advertisingService.updateById(AD_ID, request)).thenReturn(defaultAdResponse());

        mockMvc.perform(patch("/ads/{id}", AD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(AD_ID))
                .andExpect(jsonPath("$.title").value(AD_TITLE));
    }

    @Test
    void updateAdsById_Forbidden_WhenAnotherAuthorAndNotAdmin_Test() throws Exception {
        CreateOrUpdateAd request = defaultAdRequest();
        when(securityHelper.isAdmin()).thenReturn(false);
        when(advertisingService.isAnotherAuthor(AD_ID)).thenReturn(true);

        mockMvc.perform(patch("/ads/{id}", AD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isForbidden());

        verify(advertisingService, never()).updateById(anyLong(), any());
    }

    // ===== GET /ads/me =====

    @Test
    void getAdsAuthorisedUser_Success_Test() throws Exception {
        AdvertisingAllResponseDto response = new AdvertisingAllResponseDto(1, List.of(defaultAdResponse()));
        when(securityHelper.getCurrentUserId()).thenReturn(AUTHOR_ID);
        when(advertisingService.findAllByUserId(AUTHOR_ID)).thenReturn(response);

        mockMvc.perform(get("/ads/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.results[0].pk").value(AD_ID));
    }

    // ===== PATCH /ads/{id}/image =====

    @Test
    void updateAdsImage_Success_WhenOwner_Test() throws Exception {
        MockMultipartFile image = imagePart("new-image.jpg");
        when(securityHelper.isAdmin()).thenReturn(false);
        when(advertisingService.isAnotherAuthor(AD_ID)).thenReturn(false);

        mockMvc.perform(multipart("/ads/{id}/image", AD_ID)
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().string("new-image.jpg"));

        verify(advertisingService).updateAdsImage(AD_ID, image);
    }

    @Test
    void updateAdsImage_Forbidden_WhenAnotherAuthorAndNotAdmin_Test() throws Exception {
        MockMultipartFile image = imagePart("new-image.jpg");
        when(securityHelper.isAdmin()).thenReturn(false);
        when(advertisingService.isAnotherAuthor(AD_ID)).thenReturn(true);

        mockMvc.perform(multipart("/ads/{id}/image", AD_ID)
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isForbidden());

        verify(advertisingService, never()).updateAdsImage(anyLong(), any());
    }

    // ===== GET /ads/{id}/comments =====

    @Test
    void getAllCommentsByAdsId_Success_Test() throws Exception {
        CommentsAllResponseDto response = new CommentsAllResponseDto(1, List.of(defaultCommentResponse()));
        when(commentService.findByAdvertisingId(AD_ID)).thenReturn(response);

        mockMvc.perform(get("/ads/{id}/comments", AD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.results[0].pk").value(COMMENT_ID))
                .andExpect(jsonPath("$.results[0].text").value(COMMENT_TEXT));
    }

    // ===== POST /ads/{id}/comments =====

    @Test
    void addCommentToAdvertisingId_Success_Test() throws Exception {
        CommentRequestDto request = new CommentRequestDto(COMMENT_TEXT);
        User currentUser = mock(User.class);
        when(securityHelper.getCurrentUser()).thenReturn(currentUser);
        when(commentService.addCommentToAdvertisingId(currentUser, AD_ID, request))
                .thenReturn(defaultCommentResponse());

        mockMvc.perform(post("/ads/{id}/comments", AD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(COMMENT_ID))
                .andExpect(jsonPath("$.author").value(AUTHOR_ID))
                .andExpect(jsonPath("$.text").value(COMMENT_TEXT));
    }

    // ===== DELETE /ads/{adId}/comments/{commentId} =====

    @Test
    void deleteComment_Success_WhenOwner_Test() throws Exception {
        when(commentService.isAnotherAuthor(COMMENT_ID, AD_ID)).thenReturn(false);

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", AD_ID, COMMENT_ID))
                .andExpect(status().isOk());

        verify(commentService).deleteCommentByIdAndAdvertisingById(COMMENT_ID, AD_ID);
    }

    @Test
    void deleteComment_Forbidden_WhenAnotherAuthorAndNotAdmin_Test() throws Exception {
        when(commentService.isAnotherAuthor(COMMENT_ID, AD_ID)).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(false);

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", AD_ID, COMMENT_ID))
                .andExpect(status().isForbidden());

        verify(commentService, never()).deleteCommentByIdAndAdvertisingById(anyLong(), anyLong());
    }

    @Test
    void deleteComment_Success_WhenAdminDeletesAnotherAuthorsComment_Test() throws Exception {
        when(commentService.isAnotherAuthor(COMMENT_ID, AD_ID)).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(true);

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", AD_ID, COMMENT_ID))
                .andExpect(status().isOk());

        verify(commentService).deleteCommentByIdAndAdvertisingById(COMMENT_ID, AD_ID);
    }

    // ===== PATCH /ads/{adId}/comments/{commentId} =====

    @Test
    void updateComment_Success_WhenOwner_Test() throws Exception {
        CommentRequestDto request = new CommentRequestDto("Обновленный комментарий");
        CommentOneResponseDto response = new CommentOneResponseDto(
                COMMENT_ID,
                AUTHOR_ID,
                USER_IMAGE,
                USER_FIRST_NAME,
                1_700_000_000_000L,
                request.text()
        );
        when(commentService.isAnotherAuthor(COMMENT_ID, AD_ID)).thenReturn(false);
        when(commentService.updateCommentByIdAndAdvertisingById(COMMENT_ID, AD_ID, request))
                .thenReturn(response);

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", AD_ID, COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(COMMENT_ID))
                .andExpect(jsonPath("$.text").value(request.text()));
    }

    @Test
    void updateComment_Forbidden_WhenAnotherAuthorAndNotAdmin_Test() throws Exception {
        CommentRequestDto request = new CommentRequestDto("Обновленный комментарий");
        when(commentService.isAnotherAuthor(COMMENT_ID, AD_ID)).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(false);

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", AD_ID, COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isForbidden());

        verify(commentService, never()).updateCommentByIdAndAdvertisingById(anyLong(), anyLong(), any());
    }

}
