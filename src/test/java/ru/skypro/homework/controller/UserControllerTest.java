package ru.skypro.homework.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.dto.SetPasswordRequestDto;
import ru.skypro.homework.dto.UserInfoResponseDto;
import ru.skypro.homework.dto.UserUpdateInfoDto;
import ru.skypro.homework.exception.InvalidPasswordException;
import ru.skypro.homework.exception.UsernameNotFoundException;
import ru.skypro.homework.security.SecurityHelper;
import ru.skypro.homework.service.UserService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SecurityHelper securityHelper;

    @Autowired
    private ObjectMapper objectMapper;

    // ===== МЕТОД-ПОМОЩНИК ДЛЯ СОЗДАНИЯ AUTHORITIES =====

    private Collection<GrantedAuthority> createAuthorities(String... roles) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

    // ===== ТЕСТЫ ДЛЯ password_update (POST /users/set_password) =====

    @Test
    void passwordUpdate_Success_Test() throws Exception {
        SetPasswordRequestDto request = new SetPasswordRequestDto("oldPass123@", "newPass456@");

        when(securityHelper.isAuthenticated()).thenReturn(true);
        doReturn(createAuthorities("ROLE_USER")).when(securityHelper).getAuthorities();
        when(securityHelper.getCurrentUsername()).thenReturn("user@mail.com");
        doNothing().when(userService).passwordUpdate(anyString(), anyString(), anyString());

        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService, times(1)).passwordUpdate("user@mail.com", "oldPass123@", "newPass456@");
    }

    @Test
    void passwordUpdate_Unauthorized_Test() throws Exception {
        SetPasswordRequestDto request = new SetPasswordRequestDto("oldPass123@", "newPass456@");

        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).passwordUpdate(anyString(), anyString(), anyString());
    }

    @Test
    void passwordUpdate_Forbidden_WhenNoRole_Test() throws Exception {
        SetPasswordRequestDto request = new SetPasswordRequestDto("oldPass123@", "newPass456@");

        when(securityHelper.isAuthenticated()).thenReturn(true);
        doReturn(createAuthorities()).when(securityHelper).getAuthorities();

        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(userService, never()).passwordUpdate(anyString(), anyString(), anyString());
    }

    @Test
    void passwordUpdate_UserNotFound_Test() throws Exception {
        SetPasswordRequestDto request = new SetPasswordRequestDto("oldPass123@", "newPass456@");

        when(securityHelper.isAuthenticated()).thenReturn(true);
        doReturn(createAuthorities("ROLE_USER")).when(securityHelper).getAuthorities();
        when(securityHelper.getCurrentUsername()).thenReturn("notfound@mail.com");
        doThrow(new UsernameNotFoundException("User not found"))
                .when(userService).passwordUpdate(anyString(), anyString(), anyString());

        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void passwordUpdate_InvalidCurrentPassword_Test() throws Exception {
        SetPasswordRequestDto request = new SetPasswordRequestDto("wrongPass@", "newPass456@");

        when(securityHelper.isAuthenticated()).thenReturn(true);
        doReturn(createAuthorities("ROLE_USER")).when(securityHelper).getAuthorities();
        when(securityHelper.getCurrentUsername()).thenReturn("user@mail.com");
        doThrow(new InvalidPasswordException("Invalid current password"))
                .when(userService).passwordUpdate(anyString(), anyString(), anyString());

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
    }

    @Test
    void passwordUpdate_EmptyNewPassword_Test() throws Exception {
        SetPasswordRequestDto request = new SetPasswordRequestDto("oldPass123@", "");

        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===== ТЕСТЫ ДЛЯ getUsersInfo (GET /users/me) =====

    @Test
    void getUsersInfo_Success_Test() throws Exception {
        UserInfoResponseDto expected = new UserInfoResponseDto(
                1L,
                "user@mail.com",
                "John",
                "Doe",
                "+79991234567",
                Role.USER,
                "/images/avatars/user.jpg"
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.getCurrentUsername()).thenReturn("user@mail.com");
        when(userService.getUserInfo("user@mail.com")).thenReturn(expected);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@mail.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phone").value("+79991234567"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.image").value("/images/avatars/user.jpg"));
    }

    @Test
    void getUsersInfo_Unauthorized_Test() throws Exception {
        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserInfo(anyString());
    }

    @Test
    void getUsersInfo_UserNotFound_Test() throws Exception {
        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.getCurrentUsername()).thenReturn("notfound@mail.com");
        when(userService.getUserInfo(anyString()))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isNotFound());
    }

    // ===== ТЕСТЫ ДЛЯ updateUsersInfo (PATCH /users/me) =====

    @Test
    void updateUsersInfo_Success_Test() throws Exception {
        UserUpdateInfoDto request = new UserUpdateInfoDto("Johnny", "Doex", "+79998887766");
        UserUpdateInfoDto expected = new UserUpdateInfoDto("Johnny", "Doex", "+79998887766");

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.getCurrentUsername()).thenReturn("user@mail.com");
        when(userService.updateUser(anyString(), any(UserUpdateInfoDto.class))).thenReturn(expected);

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.lastName").value("Doex"))
                .andExpect(jsonPath("$.phone").value("+79998887766"));
    }

    @Test
    void updateUsersInfo_Unauthorized_Test() throws Exception {
        UserUpdateInfoDto request = new UserUpdateInfoDto("Johnny", "Doex", "+79998887766");

        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateUser(anyString(), any());
    }

    @Test
    void updateUsersInfo_EmptyFirstName_Test() throws Exception {
        UserUpdateInfoDto request = new UserUpdateInfoDto("", "Doex", "+79998887766");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUsersInfo_FirstNameWithNumbers_Test() throws Exception {
        UserUpdateInfoDto request = new UserUpdateInfoDto("John123", "Doex", "+79998887766");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUsersInfo_InvalidPhone_Test() throws Exception {
        UserUpdateInfoDto request = new UserUpdateInfoDto("Johnny", "Doex", "123");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUsersInfo_UserNotFound_Test() throws Exception {
        UserUpdateInfoDto request = new UserUpdateInfoDto("Johnny", "Doex", "+79998887766");

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.getCurrentUsername()).thenReturn("notfound@mail.com");
        when(userService.updateUser(anyString(), any(UserUpdateInfoDto.class)))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ===== ТЕСТЫ ДЛЯ updateUsersAvatar (PATCH /users/me/image) =====

    @Test
    void updateUsersAvatar_Success_Test() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake image content".getBytes()
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.getCurrentUsername()).thenReturn("user@mail.com");
        doNothing().when(userService).updateUsersAvatar(anyString(), any(MultipartFile.class));

        mockMvc.perform(multipart("/users/me/image")
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUsersAvatar("user@mail.com", image);
    }

    @Test
    void updateUsersAvatar_Unauthorized_Test() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake image content".getBytes()
        );

        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(multipart("/users/me/image")
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateUsersAvatar(anyString(), any());
    }

    @Test
    void updateUsersAvatar_IOException_Test() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake image content".getBytes()
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.getCurrentUsername()).thenReturn("user@mail.com");
        doThrow(new IOException("File processing error"))
                .when(userService).updateUsersAvatar(anyString(), any(MultipartFile.class));

        mockMvc.perform(multipart("/users/me/image")
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateUsersAvatar_EmptyFile_Test() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.getCurrentUsername()).thenReturn("user@mail.com");
        doNothing().when(userService).updateUsersAvatar(anyString(), any(MultipartFile.class));

        mockMvc.perform(multipart("/users/me/image")
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk());
    }

    @Test
    void updateUsersAvatar_InvalidFileType_Test() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "document.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "fake pdf content".getBytes()
        );

        when(securityHelper.isAuthenticated()).thenReturn(true);
        when(securityHelper.getCurrentUsername()).thenReturn("user@mail.com");
        doNothing().when(userService).updateUsersAvatar(anyString(), any(MultipartFile.class));

        mockMvc.perform(multipart("/users/me/image")
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk());
    }
}