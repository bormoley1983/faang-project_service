package faang.school.projectservice.service.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageTransactionCoordinator {
    private final S3Service s3Service;

    public void deleteAfterCommit(String key) {
        requireTransaction();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    s3Service.deleteFile(key);
                } catch (RuntimeException exception) {
                    log.error("Post-commit object cleanup failed for key {}; reconciliation is required", key, exception);
                }
            }
        });
    }

    public void deleteOnRollback(String key) {
        requireTransaction();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try {
                        s3Service.deleteFile(key);
                    } catch (RuntimeException exception) {
                        log.error("Rollback object compensation failed for key {}; reconciliation is required", key, exception);
                    }
                }
            }
        });
    }

    private void requireTransaction() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Storage coordination requires an active transaction");
        }
    }
}
