package ru.skypro.homework.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.homework.constants.ExceptionMessages;
import ru.skypro.homework.dto.AdvertisingOneResponseDto;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.exception.AdvertisingNotFoundException;
import ru.skypro.homework.security.SecurityHelper;
import ru.skypro.homework.service.AdvertisingService;
import ru.skypro.homework.service.CommentService;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

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

    // Данные для тестов объявлений
    private static final Long AD_ID = 1L;
    private static final Long NON_EXISTENT_AD_ID = -9L;
    private static final Long AUTHOR_ID = 100L;
    private static final String TITLE = "iPhone 15 Pro Max";
    private static final String DESCRIPTION = "Описание объявления";
    private static final Integer PRICE = 499;
    private static final String IMAGE_PATH = "/images/ad_1.jpg";

    // Данные для тестов пользователя
    private static final String USERNAME = "user";
    private static final String USER_EMAIL = "youmail@google.com";
    private static final String USER_FIRST_NAME = "authorFirstName";
    private static final String USER_LAST_NAME = "authorLastName";
    private static final String USER_PHONE = "+7499 123123";
    private static final String USER_IMAGE = "picture.jpg";

    // Данные для тестов создания объявления
    private static final String AD_TITLE = "Заголовок объявления";
    private static final String AD_DESCRIPTION = "Описание объявления";
    private static final Integer AD_PRICE = 123;

    // Данные для тестов удаления
    private static final Long DELETE_AD_ID = 1L;

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
        // Базовые настройки для всех тестов
        // Можно добавить общие настройки, если нужно
    }

    // ===== ТЕСТЫ =====

    @Test
    void getAdsById_Success_Test() throws Exception {
        // 1. Создаем тестовый объект
        AdvertisingWithAuthorDto expected = createDefaultAdvertisingWithAuthorDto();

        // 2. Настраиваем моки
        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(adsService.getAdById(AD_ID)).thenReturn(expected);

        // 3. Выполняем GET запрос и проверяем результат
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
    void addAds_Success_Test() throws Exception {
        // Подготовка данных
        CreateOrUpdateAd properties = createDefaultProperties();
        MockMultipartFile image = createDefaultImage();
        MockMultipartFile propertiesPart = createPropertiesPart(properties);

        AdvertisingOneResponseDto adsSavedDto = createDefaultAdvertisingOneResponseDto();

        // Настройка моков
        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);
        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(adsService.createAds(securityHelper.getCurrentUsername(), properties, image))
                .thenReturn(adsSavedDto);

        // Выполнение и проверка
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
        // Подготовка данных
        CreateOrUpdateAd properties = createDefaultProperties();
        MockMultipartFile image = createDefaultImage();
        MockMultipartFile propertiesPart = createPropertiesPart(properties);

        // Настройка моков - пользователь НЕ аутентифицирован
        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);
        when(securityHelper.isAuthenticated()).thenReturn(false);

        // Выполнение и проверка
        mockMvc.perform(multipart("/ads")
                        .file(image)
                        .file(propertiesPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }
}