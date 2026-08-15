package ru.skypro.homework.service.storage.alfresco;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import ru.skypro.homework.model.Advertising;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.AdvertisingRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.storage.FileStorageService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlfrescoCleanupJob {

    private static final String API = "/api/-default-/public/alfresco/versions/1/nodes";

    private final RestClient client;
    private final AlfrescoProperties properties;
    private final UserRepository userRepository;
    private final AdvertisingRepository advertisingRepository;
    private final FileStorageService fileService;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOrphanedFiles() {
        log.info("Starting Alfresco cleanup job...");

        try {
            Set<String> usedFileIds = getUsedFileIds();
            List<AlfrescoFileInfo> allFiles = getAllFilesFromAlfresco();

            int deletedCount = 0;
            for (AlfrescoFileInfo fileInfo : allFiles) {
                String fileId = fileInfo.getId();
                String fileName = fileInfo.getName();

                if (fileInfo.isFolder()) {
                    continue;
                }

                if (!usedFileIds.contains(fileId)) {
                    try {
                        fileService.delete(fileId);
                        deletedCount++;
                        log.debug("Deleted orphaned file: {} (ID: {})", fileName, fileId);
                    } catch (Exception e) {
                        log.error("Failed to delete orphaned file: {} (ID: {})", fileName, fileId, e);
                    }
                }
            }

            log.info("Cleanup job completed. Deleted {} orphaned files.", deletedCount);

        } catch (Exception e) {
            log.error("Cleanup job failed", e);
        }
    }

    private Set<String> getUsedFileIds() {
        Set<String> usedFileIds = new HashSet<>();

        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (StringUtils.hasText(user.getAvatarFileId())) {
                usedFileIds.add(user.getAvatarFileId());
            }
        }

        List<Advertising> advertisements = advertisingRepository.findAll();
        for (Advertising ad : advertisements) {
            if (StringUtils.hasText(ad.getImageFileId())) {
                usedFileIds.add(ad.getImageFileId());
            }
        }

        log.debug("Found {} used file IDs in database", usedFileIds.size());
        return usedFileIds;
    }

    private List<AlfrescoFileInfo> getAllFilesFromAlfresco() {
        List<AlfrescoFileInfo> allFiles = new ArrayList<>();

        try {
            String folderId = properties.folderId();
            int skipCount = 0;
            int maxItems = 100;
            boolean hasMoreItems = true;

            while (hasMoreItems) {
                String uri = API + "/{folderId}/children?skipCount={skipCount}&maxItems={maxItems}";

                AlfrescoChildrenResponse response = client.get()
                        .uri(uri, folderId, skipCount, maxItems)
                        .retrieve()
                        .body(AlfrescoChildrenResponse.class);

                if (response != null && response.getList() != null) {
                    for (AlfrescoChildrenResponse.Entry entry : response.getList().getEntries()) {
                        AlfrescoChildrenResponse.AlfrescoChildrenEntry node = entry.getEntry();
                        AlfrescoFileInfo fileInfo = new AlfrescoFileInfo(
                                node.getId(),
                                node.getName(),
                                node.isFolder()
                        );
                        allFiles.add(fileInfo);
                    }

                    hasMoreItems = response.getList().getPagination().isHasMoreItems();
                    skipCount += maxItems;
                } else {
                    hasMoreItems = false;
                }
            }

            log.info("Found {} files in Alfresco folder", allFiles.size());

        } catch (Exception e) {
            log.error("Failed to get files from Alfresco", e);
        }

        return allFiles;
    }
}