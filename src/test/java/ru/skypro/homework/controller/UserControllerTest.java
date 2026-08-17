package ru.skypro.homework.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.dto.SetPasswordRequestDto;
import ru.skypro.homework.dto.UserInfoResponseDto;
import ru.skypro.homework.dto.UserUpdateInfoDto;
import ru.skypro.homework.exception.InvalidPasswordException;
import ru.skypro.homework.security.SecurityHelper;
import ru.skypro.homework.service.UserService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private static final String USERNAME = "user@mail.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SecurityHelper securityHelper;

    //  @MockitoBean
    //   private UserDetailsService userDetailsService;

    // ===== POST /users/set_password =====

    @Test
    void passwordUpdate_Success_Test() throws Exception {
        SetPasswordRequestDto request = new SetPasswordRequestDto("oldPass123@", "newPass456@");
        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);

        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).passwordUpdate(USERNAME, "oldPass123@", "newPass456@");
    }

    @Test
    void passwordUpdate_UserNotFound_Test() throws Exception {
        SetPasswordRequestDto request = new SetPasswordRequestDto("oldPass123@", "newPass456@");
        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);
        doThrow(new UsernameNotFoundException("User not found"))
                .when(userService).passwordUpdate(USERNAME, "oldPass123@", "newPass456@");

        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void passwordUpdate_InvalidCurrentPassword_Test() throws Exception {
        SetPasswordRequestDto request = new SetPasswordRequestDto("wrongPass@", "newPass456@");
        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);
        doThrow(new InvalidPasswordException("Invalid current password"))
                .when(userService).passwordUpdate(USERNAME, "wrongPass@", "newPass456@");

        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void passwordUpdate_EmptyCurrentPassword_Test() throws Exception {
        SetPasswordRequestDto request = new SetPasswordRequestDto("", "newPass456@");

        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void passwordUpdate_EmptyNewPassword_Test() throws Exception {
        SetPasswordRequestDto request = new SetPasswordRequestDto("oldPass123@", "");

        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    // ===== GET /users/me =====

    @Test
    void getUsersInfo_Success_Test() throws Exception {
        UserInfoResponseDto expected = new UserInfoResponseDto(
                1L,
                USERNAME,
                "John",
                "Doe",
                "+79991234567",
                Role.USER,
                "/images/avatars/user.jpg"
        );
        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);
        when(userService.getUserInfo(USERNAME)).thenReturn(expected);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value(USERNAME))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phone").value("+79991234567"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.image").value("/images/avatars/user.jpg"));

        verify(userService).getUserInfo(USERNAME);
    }

    @Test
    void getUsersInfo_UserNotFound_Test() throws Exception {
        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);
        when(userService.getUserInfo(USERNAME))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isNotFound());
    }

    // ===== PATCH /users/me =====

    @Test
    void updateUsersInfo_Success_Test() throws Exception {
        UserUpdateInfoDto request = new UserUpdateInfoDto("Johnny", "Doex", "+79998887766");
        UserUpdateInfoDto expected = new UserUpdateInfoDto("Johnny", "Doex", "+79998887766");
        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);
        when(userService.updateUser(eq(USERNAME), any(UserUpdateInfoDto.class))).thenReturn(expected);

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.lastName").value("Doex"))
                .andExpect(jsonPath("$.phone").value("+79998887766"));

        verify(userService).updateUser(eq(USERNAME), any(UserUpdateInfoDto.class));
    }

    @Test
    void updateUsersInfo_EmptyFirstName_Test() throws Exception {
        UserUpdateInfoDto request = new UserUpdateInfoDto("", "Doex", "+79998887766");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void updateUsersInfo_FirstNameWithNumbers_Test() throws Exception {
        UserUpdateInfoDto request = new UserUpdateInfoDto("John123", "Doex", "+79998887766");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void updateUsersInfo_InvalidPhone_Test() throws Exception {
        UserUpdateInfoDto request = new UserUpdateInfoDto("Johnny", "Doex", "123");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void updateUsersInfo_UserNotFound_Test() throws Exception {
        UserUpdateInfoDto request = new UserUpdateInfoDto("Johnny", "Doex", "+79998887766");
        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);
        when(userService.updateUser(eq(USERNAME), any(UserUpdateInfoDto.class)))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ===== PATCH /users/me/image =====

    @Test
    void updateUsersAvatar_Success_Test() throws Exception {
        MockMultipartFile image = avatar("avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "fake image content".getBytes());
        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);

        mockMvc.perform(multipart("/users/me/image")
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk());

        verify(userService).updateUsersAvatar(eq(USERNAME), any(MultipartFile.class));
    }

    @Test
    void updateUsersAvatar_IOException_Test() throws Exception {
        MockMultipartFile image = avatar("avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "fake image content".getBytes());
        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);
        doThrow(new IOException("File processing error"))
                .when(userService).updateUsersAvatar(eq(USERNAME), any(MultipartFile.class));

        mockMvc.perform(multipart("/users/me/image")
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateUsersAvatar_EmptyFile_CurrentControllerAcceptsIt_Test() throws Exception {
        MockMultipartFile image = avatar("avatar.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);
        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);

        mockMvc.perform(multipart("/users/me/image")
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk());

        verify(userService).updateUsersAvatar(eq(USERNAME), any(MultipartFile.class));
    }

    @Test
    void updateUsersAvatar_InvalidFileType_CurrentControllerAcceptsIt_Test() throws Exception {
        MockMultipartFile image = avatar("document.pdf", MediaType.APPLICATION_PDF_VALUE, "fake pdf content".getBytes());
        when(securityHelper.getCurrentUsername()).thenReturn(USERNAME);

        mockMvc.perform(multipart("/users/me/image")
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk());

        verify(userService).updateUsersAvatar(eq(USERNAME), any(MultipartFile.class));
    }

    private MockMultipartFile avatar(String fileName, String contentType, byte[] content) {
        return new MockMultipartFile("image", fileName, contentType, content);
    }
}
