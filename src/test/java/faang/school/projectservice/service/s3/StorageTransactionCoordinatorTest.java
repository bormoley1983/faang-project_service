package faang.school.projectservice.service.s3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link StorageTransactionCoordinator}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StorageTransactionCoordinator")
class StorageTransactionCoordinatorTest {

    @Mock
    private S3Service s3Service;

    private StorageTransactionCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator = new StorageTransactionCoordinator(s3Service);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // ── deleteAfterCommit ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteAfterCommit")
    class DeleteAfterCommit {

        @Test
        @DisplayName("throws IllegalStateException when no active transaction")
        void throwsWhenNoTransaction() {
            assertThatThrownBy(() -> coordinator.deleteAfterCommit("key-1"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("requires an active transaction");
        }

        @Test
        @DisplayName("registers synchronization that deletes file after commit")
        void registersSynchronization() {
            TransactionSynchronizationManager.initSynchronization();

            coordinator.deleteAfterCommit("key-1");

            List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
            assertThat(syncs).hasSize(1);

            // Simulate commit
            syncs.get(0).afterCommit();
            verify(s3Service).deleteFile("key-1");
        }

        @Test
        @DisplayName("swallows RuntimeException from S3 delete after commit")
        void swallowsRuntimeException() {
            TransactionSynchronizationManager.initSynchronization();
            doThrow(new RuntimeException("S3 down")).when(s3Service).deleteFile(anyString());

            coordinator.deleteAfterCommit("key-1");

            List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
            assertThatCode(() -> syncs.get(0).afterCommit()).doesNotThrowAnyException();
        }
    }

    // ── deleteOnRollback ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteOnRollback")
    class DeleteOnRollback {

        @Test
        @DisplayName("throws IllegalStateException when no active transaction")
        void throwsWhenNoTransaction() {
            assertThatThrownBy(() -> coordinator.deleteOnRollback("key-1"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("requires an active transaction");
        }

        @Test
        @DisplayName("deletes file when transaction rolls back")
        void deletesFileOnRollback() {
            TransactionSynchronizationManager.initSynchronization();

            coordinator.deleteOnRollback("key-1");

            List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
            assertThat(syncs).hasSize(1);

            // Simulate rollback
            syncs.get(0).afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            verify(s3Service).deleteFile("key-1");
        }

        @Test
        @DisplayName("does not delete file when transaction commits")
        void doesNotDeleteOnCommit() {
            TransactionSynchronizationManager.initSynchronization();

            coordinator.deleteOnRollback("key-1");

            List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
            // Simulate commit (status 0)
            syncs.get(0).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
            verify(s3Service, never()).deleteFile(anyString());
        }

        @Test
        @DisplayName("swallows RuntimeException from S3 delete on rollback")
        void swallowsRuntimeException() {
            TransactionSynchronizationManager.initSynchronization();
            doThrow(new RuntimeException("S3 down")).when(s3Service).deleteFile(anyString());

            coordinator.deleteOnRollback("key-1");

            List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
            assertThatCode(() ->
                    syncs.get(0).afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK))
                    .doesNotThrowAnyException();
        }
    }
}
