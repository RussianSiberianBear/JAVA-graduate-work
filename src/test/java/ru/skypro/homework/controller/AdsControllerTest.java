package ru.skypro.homework.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Правильный импорт для Spring Boot 4
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.homework.constants.ExceptionMessages;
import ru.skypro.homework.dto.AdvertisingWithAuthorDto;
import ru.skypro.homework.exception.AdvertisingNotFoundException;
import ru.skypro.homework.security.SecurityHelper;
import ru.skypro.homework.service.AdvertisingService;
import ru.skypro.homework.service.CommentService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdsController.class)
public class AdsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean  // В Spring Boot 4 используется @MockitoBean вместо @MockBean
    private AdvertisingService adsService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private SecurityHelper securityHelper;

    @Test
        //   @WithMockUser(username = "user@mail.com", password = "password", roles = "USER")
    void getAdsById_Test() throws Exception {
        // 1. Создаем тестовый объект
        AdvertisingWithAuthorDto expected = new AdvertisingWithAuthorDto(
                1L,
                "authorFirstName",
                "authorLastName",
                "description",
                "youmail@google.com",
                "pictute.jpg",
                "+7499 123123",
                "123",
                "Корм для животных"
        );

        // 2. Настраиваем мок - когда вызывают метод getAdById(1), возвращаем наш объект
//        when(securityHelper.getCurrentUser()).thenReturn(new User(1L, "youmail@google.com", "password", "authorFirstName",
        //               "authorLastName", "+7499 123123", Role.USER, "pictute.jpg"));
        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(adsService.getAdById(1L)).thenReturn(expected);

        // 3. Выполняем GET запрос и проверяем результат
        mockMvc.perform(get("/ads/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(1L))
                .andExpect(jsonPath("$.authorFirstName").value("authorFirstName"))
                .andExpect(jsonPath("$.description").value("description"))
                .andExpect(jsonPath("$.title").value("Корм для животных"));
    }

    @Test
    void getAdsById_NotFound_Test() throws Exception {
        // Настраиваем мок
        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(adsService.getAdById(-9L)).thenThrow(new AdvertisingNotFoundException(ExceptionMessages.formatAdNotFound(-9L)));

        mockMvc.perform(get("/ads/-9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAdsById_Success_Test()  throws Exception {

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(true);
        when(adsService.isAnotherAuthor(1L)).thenReturn(false);

        doNothing().when(adsService).deleteAdById(1L);

        mockMvc.perform(delete("/ads/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAdsById_Forbidden_Test()  throws Exception {

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.isAdmin()).thenReturn(false);
        when(adsService.isAnotherAuthor(1L)).thenReturn(true);

        doNothing().when(adsService).deleteAdById(1L);

        mockMvc.perform(delete("/ads/1"))
                .andExpect(status().isForbidden());
    }
}