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
import java.time.Instant;
import java.util.Set;

@Service
@Slf4j
public class FileService {

    private static final long SHARD_SIZE = 10_000; // 10 000 пользователей в одном шарде
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    private final UserRepository userRepository;

    public FileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public boolean uploadAvatarFile(User user, MultipartFile file) throws IOException {
        validate(user.getId(), file);

        String originalName = safeOriginalName(file.getOriginalFilename());
        String extension = getFileExtension(originalName);
        String uniqueFilename = generateUniqueFilename(user.getId(), extension);

        // Сохраняем имя файла в БД
        String oldFilename = user.getAvatarFilename();
        user.setAvatarFilename(uniqueFilename);
        userRepository.save(user);

        try {
            // Сохраняем файл в шардированную директорию
            savePhysicalFile(file, user.getId(), uniqueFilename);

            // Удаляем старый файл
            if (oldFilename != null && !oldFilename.isBlank()) {
                deleteOldAvatar(user.getId(), oldFilename);
            }

            return true;
        } catch (Exception e) {
            // Откатываем изменения в БД
            user.setAvatarFilename(oldFilename);
            userRepository.save(user);

            // Удаляем загруженный файл, если он был создан
            deleteOldAvatar(user.getId(), uniqueFilename);

            if (e instanceof IOException io) {
                throw io;
            }
            throw e;
        }
    }

    public Path getPhysicalFilePath(Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь c id=" + userId + " не найден!"));

        String filename = user.getAvatarFilename();
        if (filename == null || filename.isBlank()) {
            throw new IOException("У пользователя нет аватара!");
        }

        Path filePath = getShardDirectory(userId).resolve(filename);
        if (!Files.exists(filePath)) {
            log.warn("Файл аватара не найден на диске: {}", filePath);
            // Очищаем ссылку в БД, так как файл потерян
            user.setAvatarFilename(null);
            userRepository.save(user);
            throw new IOException("Файл аватара не найден!");
        }

        return filePath;
    }

    // Удаление аватара
    @Transactional
    public boolean deleteAvatar(Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь c id=" + userId + " не найден!"));

        String filename = user.getAvatarFilename();
        if (filename == null || filename.isBlank()) {
            return false; // У пользователя нет аватара
        }

        // Удаляем файл
        deleteOldAvatar(userId, filename);

        // Очищаем ссылку в БД
        user.setAvatarFilename(null);
        userRepository.save(user);

        return true;
    }

    /**
     * Определяет директорию для пользователя на основе его ID (шардирование)
     * Например: userId=12345 -> shard 10000-19999
     */
    private Path getShardDirectory(Long userId) {
        long shardStart = (userId / SHARD_SIZE) * SHARD_SIZE;
        long shardEnd = shardStart + SHARD_SIZE - 1;
        String shardName = shardStart + "-" + shardEnd;

        return uploadRoot()
                .resolve(shardName);
    }

    private Path uploadRoot() {
        return DirectoryConfig.getAbsoluteAvatarFilePath().toAbsolutePath().normalize();
    }

    private void savePhysicalFile(MultipartFile file, Long userId, String filename) throws IOException {
        Path shardDir = getShardDirectory(userId);
        Files.createDirectories(shardDir);

        Path filePath = shardDir.resolve(filename);
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Файл сохранен: {} (размер: {} байт, пользователь: {})",
                filePath, file.getSize(), userId);
    }

    private void deleteOldAvatar(Long userId, String filename) throws IOException {
        if (filename == null || filename.isBlank()) {
            return;
        }

        Path shardDir = getShardDirectory(userId);
        Path filePath = shardDir.resolve(filename);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Старый аватар удален: {} (пользователь: {})", filePath, userId);
        } else {
            log.warn("Старый аватар не найден для удаления: {} (пользователь: {})", filePath, userId);
        }
    }

    private void validate(Long userId, MultipartFile file) {
        if (userId == null) {
            throw new IllegalArgumentException("ID пользователя не может быть null");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не выбран или пуст");
        }

        // Проверка размера
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Размер файла не должен превышать " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        // Проверка типа содержимого
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Допустимы только изображения");
        }

        // Проверка расширения
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Недопустимый формат файла. Разрешены: " + ALLOWED_EXTENSIONS);
        }
    }

    private String generateUniqueFilename(Long userId, String extension) {
        // Формат: {userId}_{timestamp}_{uuid}.{ext}
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String uuid = java.util.UUID.randomUUID().toString().substring(0, 8);
        return userId + "_" + timestamp + "_" + uuid + "." + extension;
    }

    private String safeOriginalName(String name) {
        if (name == null || name.isBlank()) {
            return "unnamed";
        }
        String clean = Paths.get(name).getFileName().toString().replaceAll("[\\p{Cntrl}]", "_");
        return clean.length() <= 255 ? clean : clean.substring(clean.length() - 255);
    }

    private String getFileExtension(String name) {
        if (name == null) {
            return "";
        }
        int index = name.lastIndexOf('.');
        return index >= 0 && index < name.length() - 1 ? name.substring(index + 1) : "";
    }

    // МЕТОДЫ ДЛЯ УПРАВЛЕНИЯ ШАРДАМИ ----

    /**
     * Очистка пустых шардов (можно запускать по расписанию)
     */
    public void cleanEmptyShards() throws IOException {
        Path root = uploadRoot();
        if (!Files.exists(root)) {
            return;
        }

        try (var paths = Files.list(root)) {
            paths.filter(Files::isDirectory)
                    .forEach(shard -> {
                        try {
                            if (isEmptyDirectory(shard)) {
                                Files.delete(shard);
                                log.info("Удалена пустая директория шарда: {}", shard);
                            }
                        } catch (IOException e) {
                            log.warn("Не удалось удалить шард: {}", shard, e);
                        }
                    });
        }
    }

    private boolean isEmptyDirectory(Path dir) throws IOException {
        try (var list = Files.list(dir)) {
            return list.findAny().isEmpty();
        }
    }

    /**
     * Получение статистики по шардам
     */
    public void printShardStats() throws IOException {
        Path root = uploadRoot();
        if (!Files.exists(root)) {
            log.info("Директория аватаров пуста");
            return;
        }

        long totalFiles = 0;
        long totalSize = 0;

        try (var paths = Files.list(root)) {
            var shards = paths.filter(Files::isDirectory).toList();

            log.info("=== СТАТИСТИКА ШАРДОВ ===");
            log.info("Всего шардов: {}", shards.size());

            for (Path shard : shards) {
                long fileCount = 0;
                long shardSize = 0;

                try (var files = Files.list(shard)) {
                    for (Path file : files.toList()) {
                        if (Files.isRegularFile(file)) {
                            fileCount++;
                            shardSize += Files.size(file);
                        }
                    }
                }

                totalFiles += fileCount;
                totalSize += shardSize;

                log.info("Шард {}: {} файлов, {} байт",
                        shard.getFileName(), fileCount, shardSize);
            }

            log.info("=== ИТОГО ===");
            log.info("Всего файлов: {}", totalFiles);
            log.info("Общий размер: {} MB", totalSize / 1024 / 1024);
        }
    }
}