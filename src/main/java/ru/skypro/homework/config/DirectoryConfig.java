package ru.skypro.homework.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DirectoryConfig {

    @Getter
    private static String avatarFilePath;

    @Getter
    private static Path absoluteAvatarFilePath;

    @PostConstruct
    public void init() throws IOException {

        if (absoluteAvatarFilePath != null) {
            Files.createDirectories(absoluteAvatarFilePath);
        }
    }

    @Value("${app.storage.file-avatar-path:${user.home}/java_graduate_work/avatar}")
    public void setAvatarFilePath(String path) {
        DirectoryConfig.avatarFilePath = path;
        DirectoryConfig.absoluteAvatarFilePath = normalizePath(path);
    }

    private static Path normalizePath(String path) {
        String normalized = path.replace('\\', '/');

        if (isAbsolutePath(normalized)) {
            return Paths.get(normalized).normalize();
        }

        return Paths.get(System.getProperty("user.dir"), normalized).normalize();
    }

    private static boolean isAbsolutePath(String path) {
        if (path.startsWith("/")) {
            return true;
        }
        return path.matches("^[A-Za-z]:[/\\\\].*");
    }

}
