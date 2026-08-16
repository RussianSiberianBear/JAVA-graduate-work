package ru.skypro.homework.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.homework.exception.FileStorageException;
import ru.skypro.homework.service.storage.FileStorageService;
import ru.skypro.homework.service.storage.StoredFile;
import ru.skypro.homework.service.storage.StoredFileInfo;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageController.class)
public class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    private static final String FILE_ID = "test-file-id";
    private static final String FILE_NAME = "image.jpg";
    private static final String CONTENT_TYPE = MediaType.IMAGE_JPEG_VALUE;
    private static final byte[] IMAGE_CONTENT = {1, 2, 3, 4, 5};

    @Test
    void getImage_Success_Test() throws Exception {
        StoredFileInfo fileInfo = new StoredFileInfo(
                FILE_ID,
                FILE_NAME,
                CONTENT_TYPE,
                IMAGE_CONTENT.length
        );
        StoredFile storedFile = new StoredFile(fileInfo, IMAGE_CONTENT);

        when(fileStorageService.get(FILE_ID)).thenReturn(storedFile);

        mockMvc.perform(get("/images/{fileId}", FILE_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + FILE_NAME + "\""
                ))
                .andExpect(header().string(
                        HttpHeaders.CACHE_CONTROL,
                        containsString("max-age=2592000")
                ))
                .andExpect(header().string(
                        HttpHeaders.CACHE_CONTROL,
                        containsString("public")
                ))
                .andExpect(content().bytes(IMAGE_CONTENT));

        verify(fileStorageService, times(1)).get(FILE_ID);
    }

    @Test
    void getImage_FileStorageError_Test() throws Exception {
        when(fileStorageService.get(FILE_ID))
                .thenThrow(new FileStorageException("Failed to load file"));

        mockMvc.perform(get("/images/{fileId}", FILE_ID))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("File storage service error")));

        verify(fileStorageService, times(1)).get(FILE_ID);
    }
}
