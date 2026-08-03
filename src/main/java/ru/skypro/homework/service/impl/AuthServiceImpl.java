package ru.skypro.homework.service.impl;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserDetailsManager manager;
    private final PasswordEncoder encoder;

    public AuthServiceImpl(UserDetailsManager manager,
                           PasswordEncoder passwordEncoder) {
        this.manager = manager;
        this.encoder = passwordEncoder;
    }

    /**
     * Метод аутентификации пользователя
     * @param userName - логин пользователя
     * @param password - пароль пользователя
     * @return boolean -  true  при спешной аутентификации и false при неуспешной
     */
    @Override
    public boolean login(String userName, String password) {
        if (!manager.userExists(userName)) {
            return false;
        }
        UserDetails userDetails = manager.loadUserByUsername(userName);
        return encoder.matches(password, userDetails.getPassword());
    }

    /**
     * Метод регистрации нового пользователя
     * @param register DTO с данными пользователя
     * @return boolean true при успешной регистрации и false при ошибке регистрации
     */
    @Override
    public boolean register(Register register) {
        if (manager.userExists(register.username())) {
            return false;
        }
        manager.createUser(
                User.builder()
                        .passwordEncoder(this.encoder::encode)
                        .password(register.password())
                        .username(register.username())
                        .roles(register.role().name())
                        .build());
        return true;
    }

}
