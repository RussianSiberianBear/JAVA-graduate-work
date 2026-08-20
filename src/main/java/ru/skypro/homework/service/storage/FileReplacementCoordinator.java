package ru.skypro.homework.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

/**
 * Координатор операций замены и отложенного удаления файлов в рамках транзакции.
 * <p>
 * Обеспечивает согласованность данных и хранилища файлов:
 * - при замене файла старый файл удаляется только после успешного коммита транзакции;
 * - при откате транзакции новый (промежуточный) файл удаляется, чтобы не оставлять «сиротские» файлы;
 * - позволяет запланировать удаление файла после коммита (например, при удалении сущности, ссылающейся на файл).
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileReplacementCoordinator {

    private final FileStorageService fileService;

    /**
     * Загружает новый файл и регистрирует удаление старого после коммита транзакции.
     * <p>
     * Если транзакция откатится, новый файл будет удалён в afterCompletion.
     * Старый файл будет удалён только после успешного коммита (afterCommit).
     * </p>
     *
     * @param request   запрос на загрузку файла
     * @param oldFileId идентификатор старого файла (может быть null или пустым — тогда удаление не планируется)
     * @return информация о загруженном новом файле ({@link StoredFileInfo})
     */
    public StoredFileInfo uploadAndRegisterReplacement(
            FileUploadRequest request,
            String oldFileId
    ) {
        StoredFileInfo newFile = fileService.upload(request);

        registerCleanup(oldFileId, newFile.id());

        return newFile;
    }

    /**
     * Регистрирует логику очистки файлов (удаление старого при успехе, нового при откате)
     * как транзакционную синхронизацию.
     * <p>
     * Метод требует активной транзакции: если её нет, выбрасывается {@link IllegalStateException}.
     * </p>
     *
     * @param oldFileId идентификатор файла, который нужно удалить после коммита
     * @param newFileId   идентификатор файла, который нужно удалить при откате (если он стал «сиротой»)
     */
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
                        // Удаляем старый файл только при успешном коммите
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
                        // При откате транзакции удаляем новый файл, чтобы не оставить «сироту»
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

    /**
     * Планирует удаление файла после успешного завершения транзакции.
     * <p>
     * Используется, например, при удалении сущности (пользователя, объявления),
     * которая ссылается на файл: файл удаляется не сразу, а после коммита,
     * чтобы избежать рассинхронизации в случае отката.
     * </p>
     *
     * @param fileId идентификатор файла для удаления после коммита
     */
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
