package ru.skypro.homework.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.config.DirectoryConfig;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class FileService {

    private final UserRepository userRepository;

    public FileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public boolean uploadAvatarFile(User user, MultipartFile file) throws IOException {
        validate(user.getId(), file);
        String originalName = safeOriginalName(file.getOriginalFilename());
        Long userId = user.getId();
        user.setAvatarFilename(originalName);
        userRepository.save(user);

        try {
            deleteStorageDirectory(userId);
            savePhysicalFile(file, userId);
            return true;
        } catch (Exception e) {
            deleteStorageDirectory(userId);
            user.setAvatarFilename(null);
            if (e instanceof IOException io) {
                throw io;
            }
            throw e;
        }
    }

    public Path getPhysicalFilePath(Long userId) throws IOException {

        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("Пользователь c id=" + userId + " не найден!"));
        Path file = storageDirectory(userId).resolve(user.getAvatarFilename());
        if (Files.exists(file)) {
            throw new IOException("Файла не существует!");
        }

        return file;
    }

    private Path storageDirectory(Long userId) {
        return uploadRoot()
                .resolve("users")
                .resolve(String.valueOf(userId));
    }

    private Path uploadRoot() {
        return DirectoryConfig.getAbsoluteAvatarFilePath().toAbsolutePath().normalize();
    }

    public Path savePhysicalFile(MultipartFile file, Long userId) throws IOException {
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        Path target = storageDirectory(userId);
        Files.createDirectories(target);

        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    private void validate(Long userId, MultipartFile file) {
        if (userId == null) throw new IllegalArgumentException("Не найден ID пользователя");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Файл не выбран или пуст");
    }

    private String safeOriginalName(String name) {
        if (name == null || name.isBlank()) return "unnamed";
        String clean = Paths.get(name).getFileName().toString().replaceAll("[\\p{Cntrl}]", "_");
        return clean.length() <= 255 ? clean : clean.substring(clean.length() - 255);
    }

    public String getFileExtension(String name) {
        if (name == null) return "";
        int index = name.lastIndexOf('.');
        return index >= 0 && index < name.length() - 1 ? name.substring(index + 1) : "";
    }

    private void deleteStorageDirectory(Long userId) {
        deleteRecursively(storageDirectory(userId));
    }

    private void deleteRecursively(Path root) {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            paths.sorted((a, b) -> b.getNameCount() - a.getNameCount()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.warn("Не удалось удалить {}", path, e);
                }
            });
        } catch (IOException e) {
            log.warn("Не удалось очистить {}", root, e);
        }
    }
}
