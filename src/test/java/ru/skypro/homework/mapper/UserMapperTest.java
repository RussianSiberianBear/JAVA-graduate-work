package ru.skypro.homework.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.dto.UserInfoResponseDto;
import ru.skypro.homework.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "user@mail.com";
    private static final String PASSWORD = "Password123@";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String PHONE = "+79991234567";
    private static final Role USER_ROLE = Role.USER;
    private static final String AVATAR_FILE_ID = "avatar123";
    private static final String EXPECTED_IMAGE_URL = "/images/avatar123";

    private Register createDefaultRegister() {
        return new Register(
                USERNAME,
                PASSWORD,
                FIRST_NAME,
                LAST_NAME,
                PHONE,
                USER_ROLE
        );
    }

    private User createDefaultUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USERNAME);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setPhone(PHONE);
        user.setRole(USER_ROLE);
        user.setAvatarFileId(AVATAR_FILE_ID);
        return user;
    }

    @Test
    void toEntity_ShouldMapRegisterToUser() {
        Register register = createDefaultRegister();

        User result = userMapper.toEntity(register);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(USERNAME);
        assertThat(result.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(result.getLastName()).isEqualTo(LAST_NAME);
        assertThat(result.getPhone()).isEqualTo(PHONE);
        assertThat(result.getRole()).isEqualTo(USER_ROLE);
        // ✅ Пароль игнорируется - должен быть null
        assertThat(result.getPassword()).isNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getAvatarFileId()).isNull();
    }

    @Test
    void toEntity_ShouldMapAdminRole_Test() {
        Register register = new Register(
                "admin@mail.com",
                "Admin123@",
                "Admin",
                "Adminov",
                "+79998887766",
                Role.ADMIN
        );

        User result = userMapper.toEntity(register);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("admin@mail.com");
        assertThat(result.getFirstName()).isEqualTo("Admin");
        assertThat(result.getLastName()).isEqualTo("Adminov");
        assertThat(result.getPhone()).isEqualTo("+79998887766");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        assertThat(result.getPassword()).isNull();
    }

    @Test
    void toResponse_ShouldMapUserToResponseDto() {
        User user = createDefaultUser();

        UserInfoResponseDto result = userMapper.toResponse(user);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.email()).isEqualTo(USERNAME);
        assertThat(result.firstName()).isEqualTo(FIRST_NAME);
        assertThat(result.lastName()).isEqualTo(LAST_NAME);
        assertThat(result.phone()).isEqualTo(PHONE);
        assertThat(result.role()).isEqualTo(USER_ROLE);
        assertThat(result.image()).isEqualTo(EXPECTED_IMAGE_URL);
    }

    @Test
    void toResponse_ShouldHandleNullAvatarFileId_Test() {
        User user = createDefaultUser();
        user.setAvatarFileId(null);

        UserInfoResponseDto result = userMapper.toResponse(user);

        assertThat(result).isNotNull();
        assertThat(result.image()).isNull();
    }

    @Test
    void toResponse_ShouldHandleNullUser_Test() {
        UserInfoResponseDto result = userMapper.toResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toImageUrl_ShouldConvertFileIdToUrl() {
        String result = ImageMapperUtil.toImageUrl(AVATAR_FILE_ID);
        assertThat(result).isEqualTo(EXPECTED_IMAGE_URL);
    }

    @Test
    void toImageUrl_ShouldReturnNullForNullFileId() {
        String result = ImageMapperUtil.toImageUrl(null);
        assertThat(result).isNull();
    }
}