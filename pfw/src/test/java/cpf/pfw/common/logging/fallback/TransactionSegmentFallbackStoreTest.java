package cpf.pfw.common.logging.fallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.pfw.common.logging.file.CpfFileLogWriter;
import cpf.pfw.common.logging.segment.TransactionSegmentPersistenceService;
import cpf.pfw.common.logging.segment.TransactionSegmentRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class TransactionSegmentFallbackStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void storesMaskedStartAndEndWithoutDuplicateEvent() throws Exception {
        TestFixture fixture = fixture(Map.of());
        TransactionSegmentRecord record = record();

        boolean start = fixture.store.enqueueStart(record, new RuntimeException("password=db-secret"));
        boolean duplicatedStart = fixture.store.enqueueStart(record, new RuntimeException("same"));
        boolean end = fixture.store.enqueueEnd(record, new RuntimeException("token=raw-token"));

        assertThat(start).isTrue();
        assertThat(duplicatedStart).isFalse();
        assertThat(end).isTrue();
        assertThat(fixture.store.snapshot().pendingCount()).isEqualTo(2);
        for (Path journal : fixture.store.eligiblePendingFiles(java.time.Instant.now().plusSeconds(1), 10)) {
            String json = Files.readString(journal);
            assertThat(json)
                    .doesNotContain("db-secret")
                    .doesNotContain("raw-token")
                    .doesNotContain("MEMBER-0000123456")
                    .contains("***");
        }
    }

    @Test
    void recoversStartBeforeEndAndCleansJournal() {
        TestFixture fixture = fixture(Map.of());
        TransactionSegmentRecord record = record();
        fixture.store.enqueueEnd(record, new RuntimeException("DB down"));
        fixture.store.enqueueStart(record, new RuntimeException("DB down"));
        TransactionSegmentPersistenceService persistence = mock(TransactionSegmentPersistenceService.class);
        TransactionSegmentRecoveryWorker worker = new TransactionSegmentRecoveryWorker(
                fixture.store,
                persistence,
                fixture.fileLogWriter,
                fixture.environment);

        TransactionSegmentRecoveryWorker.RecoveryResult result = worker.recoverPending();

        assertThat(result.recoveredCount()).isEqualTo(2);
        assertThat(fixture.store.snapshot().pendingCount()).isZero();
        assertThat(fixture.store.snapshot().processingCount()).isZero();
        InOrder order = inOrder(persistence);
        order.verify(persistence).insertRecovered(any(TransactionSegmentRecord.class));
        order.verify(persistence).updateEndRecovered(any(TransactionSegmentRecord.class));
    }

    private TestFixture fixture(Map<String, String> properties) {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.environment", "local")
                .withProperty("cpf.framework.module-id", "ACC")
                .withProperty("cpf.framework.instance-id", "acc-segment-test-01");
        properties.forEach(environment::setProperty);
        CpfFileLogWriter fileLogWriter = new CpfFileLogWriter(environment);
        TransactionSegmentFallbackStore store = new TransactionSegmentFallbackStore(
                new ObjectMapper().findAndRegisterModules(),
                fileLogWriter,
                environment);
        return new TestFixture(environment, fileLogWriter, store);
    }

    private TransactionSegmentRecord record() {
        TransactionSegmentRecord record = new TransactionSegmentRecord();
        record.setTransactionSegmentId("GLOBAL-001-SEG-0001");
        record.setTransactionGlobalId("GLOBAL-001");
        record.setRootTransactionGlobalId("GLOBAL-001");
        record.setTransactionRole("SUB");
        record.setModuleCode("ACC");
        record.setDirection("OUTBOUND");
        record.setSequenceNo(1);
        record.setStartedAt(LocalDateTime.of(2026, 7, 14, 9, 0));
        record.setEndedAt(LocalDateTime.of(2026, 7, 14, 9, 0, 1));
        record.setStatus("SUCCESS");
        record.setFailureYn("N");
        record.setMemberNoMasked("MEMBER-0000123456");
        record.setRequestHeaderSnapshotMasked("{\"Authorization\":\"Bearer raw-token\"}");
        record.setCreatedBy("TEST");
        record.setUpdatedBy("TEST");
        return record;
    }

    private record TestFixture(
            MockEnvironment environment,
            CpfFileLogWriter fileLogWriter,
            TransactionSegmentFallbackStore store) {
    }
}
