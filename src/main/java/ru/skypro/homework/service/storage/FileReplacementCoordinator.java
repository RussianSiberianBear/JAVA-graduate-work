package ru.skypro.homework.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileReplacementCoordinator {

    private final FileStorageService fileService;

    public StoredFileInfo uploadAndRegisterReplacement(
            FileUploadRequest request,
            String oldFileId
    ) {
        StoredFileInfo newFile = fileService.upload(request);

        registerCleanup(oldFileId, newFile.id());

        return newFile;
    }

    private void registerCleanup(
            String oldFileId,
            String newFileId
    ) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException(
                    "Transaction synchronization is not active"
            );
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        if (!StringUtils.hasText(oldFileId)) {
                            return;
                        }

                        try {
                            fileService.delete(oldFileId);
                            log.info(
                                    "Old file deleted after transaction commit: {}",
                                    oldFileId
                            );
                        } catch (Exception e) {
                            log.warn(
                                    "Failed to delete old file: {}. " +
                                            "Scheduled cleanup will remove it later.",
                                    oldFileId,
                                    e
                            );
                        }
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_ROLLED_BACK) {
                            return;
                        }

                        try {
                            fileService.delete(newFileId);
                            log.info(
                                    "New file deleted after transaction rollback: {}",
                                    newFileId
                            );
                        } catch (Exception e) {
                            log.error(
                                    "Failed to delete new orphan file: {}. " +
                                            "Scheduled cleanup will remove it later.",
                                    newFileId,
                                    e
                            );
                        }
                    }
                }
        );
    }

    public void deleteAfterCommit(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException(
                    "Transaction synchronization is not active"
            );
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        try {
                            fileService.delete(fileId);

                            log.info(
                                    "File deleted after transaction commit: {}",
                                    fileId
                            );
                        } catch (Exception e) {
                            log.warn(
                                    "Failed to delete file after transaction commit: {}. " +
                                            "Scheduled cleanup will remove it later.",
                                    fileId,
                                    e
                            );
                        }
                    }
                }
        );
    }
}