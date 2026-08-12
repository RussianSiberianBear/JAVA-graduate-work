package ru.skypro.homework.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.exception.UsernameNotFoundException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    public AuthServiceImpl(UserDetailsService userDetailsService, UserRepository userRepository, UserMapper userMapper,
                           PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.encoder = passwordEncoder;
    }

    /**
     * Метод аутентификации пользователя
     *
     * @param userName - логин пользователя
     * @param password - пароль пользователя
     * @return boolean -  true  при спешной аутентификации и false при неуспешной
     */
    @Override
    public boolean login(String userName, String password) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
            return encoder.matches(password, userDetails.getPassword());
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    /**
     * Метод регистрации нового пользователя
     *
     * @param register DTO с данными пользователя
     * @return boolean true при успешной регистрации и false при ошибке регистрации
     */
    @Override
    public boolean register(Register register) {
        try {
            // прверяем и не допускаем регистрации с одинаковым username(у нас это e-mail)
            userDetailsService.loadUserByUsername(register.username());
            return false;
        } catch (UsernameNotFoundException e) {
            User user = userMapper.toEntity(register);
            user.setPassword(encoder.encode(register.password()));
            userRepository.save(userMapper.toEntity(register));
            return true;
        }
    }

}
