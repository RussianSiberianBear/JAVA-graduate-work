package ru.skypro.homework.service.storage.alfresco;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import ru.skypro.homework.repository.AdvertisingRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.storage.FileStorageService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Компонент для периодической очистки неиспользуемых файлов в Alfresco.
 * <p>
 * Задача ({@link Scheduled}) запускается ежедневно в 03:00. Она:
 * - собирает список всех файлов в заданной папке Alfresco (с пагинацией);
 * - получает из БД список ID файлов, которые сейчас используются (аватары пользователей, изображения объявлений);
 * - удаляет файлы, которые:
 *   - не используются в приложении;
 *   - старше заданного TTL (по умолчанию — 1 день);
 *   - являются файлами (не папками).
 * </p>
 * <p>
 * Логика защищает от удаления «свежих» сиротских файлов (например, если транзакция ещё не завершена
 * или файл временно не привязан к сущности) и от ошибок при удалении — в случае сбоя файл
 * будет обработан на следующем запуске.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlfrescoCleanupJob {

    private static final String API =
            "/api/-default-/public/alfresco/versions/1/nodes";

    private static final int PAGE_SIZE = 100;

    /**
     * Время «жизни» неиспользуемого файла до удаления.
     * <p>
     * Файлы, созданные менее суток назад и не привязанные к сущностям, не удаляются,
     * чтобы избежать проблем при незавершённых операциях загрузки/привязки.
     * </p>
     */
    private static final Duration ORPHAN_FILE_TTL =
            Duration.ofDays(1);

    private final RestClient client;
    private final AlfrescoProperties properties;
    private final UserRepository userRepository;
    private final AdvertisingRepository advertisingRepository;
    private final FileStorageService fileService;

    /**
     * Ежедневная задача очистки «сиротских» файлов в Alfresco.
     * <p>
     * Cron-выражение «0 0 3 * * *» означает запуск каждый день в 03:00.
     * </p>
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOrphanedFiles() {
        log.info("Starting Alfresco cleanup job...");

        try {
            Set<String> usedFileIds = getUsedFileIds();
            List<AlfrescoFileInfo> allFiles = getAllFilesFromAlfresco();

            OffsetDateTime deleteBefore =
                    OffsetDateTime.now().minus(ORPHAN_FILE_TTL);

            int deletedCount = 0;
            int skippedRecentCount = 0;

            for (AlfrescoFileInfo fileInfo : allFiles) {
                // Пропускаем папки — удаляем только файлы
                if (fileInfo.isFolder()) {
                    continue;
                }

                String fileId = fileInfo.getId();

                // Пропускаем файлы без валидного ID
                if (!StringUtils.hasText(fileId)) {
                    continue;
                }

                // Если файл используется в БД — не удаляем
                if (usedFileIds.contains(fileId)) {
                    continue;
                }

                OffsetDateTime createdAt = fileInfo.getCreatedAt();

                // Если нет даты создания — пропускаем (не можем корректно проверить возраст)
                if (createdAt == null) {
                    log.warn(
                            "Skipping orphaned file because createdAt is missing: {} (ID: {})",
                            fileInfo.getName(),
                            fileId
                    );
                    continue;
                }

                // Если файл создан недавно (младше TTL) — не удаляем, чтобы избежать рассинхронизации
                if (createdAt.isAfter(deleteBefore)) {
                    skippedRecentCount++;

                    log.debug(
                            "Skipping recent orphaned file: {} (ID: {}, createdAt: {})",
                            fileInfo.getName(),
                            fileId,
                            createdAt
                    );

                    continue;
                }

                // Удаляем старый неиспользуемый файл
                try {
                    fileService.delete(fileId);
                    deletedCount++;

                    log.info(
                            "Deleted orphaned file older than {}: {} (ID: {}, createdAt: {})",
                            ORPHAN_FILE_TTL,
                            fileInfo.getName(),
                            fileId,
                            createdAt
                    );
                } catch (Exception e) {
                    log.error(
                            "Failed to delete orphaned file: {} (ID: {})",
                            fileInfo.getName(),
                            fileId,
                            e
                    );
                }
            }

            log.info(
                    "Cleanup job completed. Found {} nodes, {} file IDs are used, " +
                            "skipped {} recent orphaned files, deleted {} old orphaned files.",
                    allFiles.size(),
                    usedFileIds.size(),
                    skippedRecentCount,
                    deletedCount
            );

        } catch (Exception e) {
            log.error("Cleanup job failed", e);
        }
    }

    /**
     * Собирает все ID файлов, которые используются в приложении.
     * <p>
     * На текущий момент учитываются:
     * - аватары пользователей (через {@code userRepository.findAllAvatarFileIds()});
     * - изображения объявлений (через {@code advertisingRepository.findAllImageFileIds()}).
     * </p>
     *
     * @return множество ID используемых файлов
     */
    private Set<String> getUsedFileIds() {
        Set<String> usedFileIds = new HashSet<>();

        userRepository.findAllAvatarFileIds().stream()
                .filter(StringUtils::hasText)
                .forEach(usedFileIds::add);

        advertisingRepository.findAllImageFileIds().stream()
                .filter(StringUtils::hasText)
                .forEach(usedFileIds::add);

        log.debug(
                "Found {} used file IDs in database",
                usedFileIds.size()
        );

        return usedFileIds;
    }

    /**
     * Получает все узлы (файлы и папки) из целевой папки Alfresco с использованием пагинации.
     * <p>
     * Выполняет последовательные запросы к Alfresco API с шагом {@code PAGE_SIZE}, пока
     * не будут получены все элементы. Для каждого элемента создаётся объект
     * {@link AlfrescoFileInfo}.
     * </p>
     *
     * @return список информации обо всех узлах в папке Alfresco
     */
    private List<AlfrescoFileInfo> getAllFilesFromAlfresco() {
        List<AlfrescoFileInfo> allFiles = new ArrayList<>();

        String folderId = properties.folderId();
        int skipCount = 0;
        boolean hasMoreItems = true;

        while (hasMoreItems) {
            String uri =
                    API + "/{folderId}/children"
                            + "?skipCount={skipCount}"
                            + "&maxItems={maxItems}";

            AlfrescoChildrenResponse response = client.get()
                    .uri(
                            uri,
                            folderId,
                            skipCount,
                            PAGE_SIZE
                    )
                    .retrieve()
                    .body(AlfrescoChildrenResponse.class);

            if (response == null || response.getList() == null) {
                break;
            }

            for (AlfrescoChildrenResponse.Entry entry
                    : response.getList().getEntries()) {

                AlfrescoChildrenResponse.AlfrescoChildrenEntry node =
                        entry.getEntry();

                allFiles.add(
                        new AlfrescoFileInfo(
                                node.getId(),
                                node.getName(),
                                node.isFolder(),
                                node.getCreatedAt()
                        )
                );
            }

            hasMoreItems =
                    response.getList()
                            .getPagination()
                            .isHasMoreItems();

            skipCount += PAGE_SIZE;
        }

        log.info(
                "Found {} nodes in Alfresco folder",
                allFiles.size()
        );

        return allFiles;
    }
}
