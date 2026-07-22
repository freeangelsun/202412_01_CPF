package cpf.pfw.common.logging.fallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.pfw.common.logging.TransactionLogRecord;
import cpf.pfw.common.logging.file.CpfFileLogWriter;
import cpf.pfw.service.common.logging.TransactionLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TransactionLogFallbackStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void storesMaskedJournalOnceForSameRecoveryEvent() throws Exception {
        TestFixture fixture = fixture(Map.of());
        TransactionLogRecord record = record();

        boolean first = fixture.store.enqueue(
                record,
                Map.of(
                        "Authorization", "Bearer original-token",
                        "requestBody", "{\"password\":\"plain-password\",\"name\":\"홍길동\"}"),
                null,
                new IllegalStateException("DB password=database-secret"));
        boolean duplicate = fixture.store.enqueue(record, Map.of(), null, null);

        assertThat(first).isTrue();
        assertThat(duplicate).isFalse();
        assertThat(fixture.store.pendingFiles()).hasSize(1);
        String journal = Files.readString(fixture.store.pendingFiles().getFirst());
        assertThat(journal)
                .contains("\"recoveryEventId\"")
                .contains("***")
                .doesNotContain("original-token")
                .doesNotContain("plain-password")
                .doesNotContain("database-secret")
                .doesNotContain("MBR-0000123456");
        assertThat(fixture.store.snapshot().health()).isEqualTo("DEGRADED");
    }

    @Test
    void recoveryWorkerPersistsAndCleansJournal() {
        TestFixture fixture = fixture(Map.of());
        fixture.store.enqueue(record(), Map.of("result", "정상"), null, new RuntimeException("DB down"));
        TransactionLogService logService = mock(TransactionLogService.class);
        TransactionLogRecoveryWorker worker = new TransactionLogRecoveryWorker(
                fixture.store,
                logService,
                fixture.fileLogWriter,
                fixture.environment);

        TransactionLogRecoveryWorker.RecoveryResult result = worker.recoverPending();

        assertThat(result.recoveredCount()).isEqualTo(1);
        assertThat(result.failedCount()).isZero();
        assertThat(fixture.store.snapshot().pendingCount()).isZero();
        assertThat(fixture.store.snapshot().processingCount()).isZero();
        verify(logService).saveTransactionLog(any(), any(), any());
    }

    @Test
    void recoveryWorkerMovesExceededRetryToPoison() {
        TestFixture fixture = fixture(Map.of("cpf.logging.db-fallback.max-attempts", "1"));
        fixture.store.enqueue(record(), Map.of(), null, new RuntimeException("DB down"));
        TransactionLogService logService = mock(TransactionLogService.class);
        doThrow(new IllegalStateException("still down"))
                .when(logService)
                .saveTransactionLog(any(), any(), any());
        TransactionLogRecoveryWorker worker = new TransactionLogRecoveryWorker(
                fixture.store,
                logService,
                fixture.fileLogWriter,
                fixture.environment);

        TransactionLogRecoveryWorker.RecoveryResult result = worker.recoverPending();

        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(fixture.store.snapshot().pendingCount()).isZero();
        assertThat(fixture.store.snapshot().poisonCount()).isEqualTo(1);
    }

    @Test
    void notReadyJournalDoesNotConsumeRecoveryBatchQuota() throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-07-14T00:00:00Z"), ZoneOffset.UTC);
        TestFixture fixture = fixture(Map.of("cpf.logging.db-fallback.recovery-batch-size", "1"), clock);
        fixture.store.enqueue(record(1), Map.of(), null, new RuntimeException("DB down"));
        fixture.store.enqueue(record(2), Map.of(), null, new RuntimeException("DB down"));

        Path deferredPath = fixture.store.pendingFiles().getFirst();
        TransactionLogFallbackEnvelope deferred = fixture.store.claim(deferredPath);
        fixture.store.retry(deferred.nextAttempt(1, clock.instant().plusSeconds(300), "WAIT"));

        TransactionLogService logService = mock(TransactionLogService.class);
        TransactionLogRecoveryWorker worker = new TransactionLogRecoveryWorker(
                fixture.store,
                logService,
                fixture.fileLogWriter,
                fixture.environment,
                clock);

        TransactionLogRecoveryWorker.RecoveryResult result = worker.recoverPending();

        assertThat(result.claimedCount()).isEqualTo(1);
        assertThat(result.recoveredCount()).isEqualTo(1);
        assertThat(fixture.store.snapshot().pendingCount()).isEqualTo(1);
    }

    @Test
    void reclaimsOnlyStaleProcessingLease() throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-07-14T00:00:00Z"), ZoneOffset.UTC);
        TestFixture fixture = fixture(Map.of(), clock);
        fixture.store.enqueue(record(), Map.of(), null, new RuntimeException("DB down"));
        fixture.store.claim(fixture.store.pendingFiles().getFirst());

        int activeReclaimed = fixture.store.reclaimStaleProcessing(clock.instant(), Duration.ofMinutes(2));
        int staleReclaimed = fixture.store.reclaimStaleProcessing(
                clock.instant().plus(Duration.ofMinutes(3)),
                Duration.ofMinutes(2));

        assertThat(activeReclaimed).isZero();
        assertThat(staleReclaimed).isEqualTo(1);
        assertThat(fixture.store.snapshot().processingCount()).isZero();
        assertThat(fixture.store.snapshot().pendingCount()).isEqualTo(1);
        assertThat(fixture.store.snapshot().staleReclaimedCount()).isEqualTo(1);
    }

    @Test
    void atomicClaimPreventsSameJournalFromBeingClaimedTwice() throws Exception {
        TestFixture fixture = fixture(Map.of());
        fixture.store.enqueue(record(), Map.of(), null, new RuntimeException("DB down"));
        Path pending = fixture.store.pendingFiles().getFirst();

        fixture.store.claim(pending);

        assertThatThrownBy(() -> fixture.store.claim(pending)).isInstanceOf(java.io.IOException.class);
        assertThat(fixture.store.snapshot().processingCount()).isEqualTo(1);
    }

    @Test
    void approvedPoisonReturnsToPendingQueue() {
        TestFixture fixture = fixture(Map.of("cpf.logging.db-fallback.max-attempts", "1"));
        fixture.store.enqueue(record(), Map.of(), null, new RuntimeException("DB down"));
        TransactionLogService logService = mock(TransactionLogService.class);
        doThrow(new IllegalStateException("still down"))
                .when(logService)
                .saveTransactionLog(any(), any(), any());
        TransactionLogRecoveryWorker worker = new TransactionLogRecoveryWorker(
                fixture.store,
                logService,
                fixture.fileLogWriter,
                fixture.environment);
        worker.recoverPending();
        String recoveryEventId = fixture.store.snapshot().poisonCount() == 1
                ? poisonEventId(fixture)
                : "missing";

        boolean xyzepted = worker.retryPoison(recoveryEventId);

        assertThat(xyzepted).isTrue();
        assertThat(fixture.store.snapshot().poisonCount()).isZero();
        assertThat(fixture.store.snapshot().pendingCount()).isEqualTo(1);
        assertThat(fixture.store.snapshot().poisonRetryCount()).isEqualTo(1);
    }

    @Test
    void rejectsJournalWhenSpoolCapacityIsExceeded() {
        TestFixture fixture = fixture(Map.of("cpf.logging.db-fallback.max-spool-bytes", "1"));

        assertThatThrownBy(() -> fixture.store.enqueue(
                record(),
                Map.of("requestBody", "large-body"),
                null,
                new RuntimeException("DB down")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("journal");
        assertThat(fixture.store.snapshot().pendingCount()).isZero();
        assertThat(fixture.store.snapshot().enqueueFailureCount()).isEqualTo(1);
    }

    private TestFixture fixture(Map<String, String> additionalProperties) {
        return fixture(additionalProperties, Clock.systemUTC());
    }

    private TestFixture fixture(Map<String, String> additionalProperties, Clock clock) {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.environment", "local")
                .withProperty("cpf.framework.module-id", "XYZ")
                .withProperty("cpf.framework.instance-id", "xyz-test-01");
        additionalProperties.forEach(environment::setProperty);
        CpfFileLogWriter fileLogWriter = new CpfFileLogWriter(environment);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        TransactionLogFallbackStore store = new TransactionLogFallbackStore(
                objectMapper,
                fileLogWriter,
                environment,
                clock);
        return new TestFixture(environment, fileLogWriter, store);
    }

    private TransactionLogRecord record() {
        return record(1);
    }

    private TransactionLogRecord record(int sequenceNo) {
        return TransactionLogRecord.builder()
                .transactionId("20260713120000000XYZxyzAP01000000" + sequenceNo)
                .spanId("SEG-000" + sequenceNo)
                .sequenceNo(sequenceNo)
                .businessTransactionId("XYZ01TST0001")
                .businessTransactionName("fallback 검증 거래")
                .logType("TRANSACTION")
                .memberNo("MBR-0000123456")
                .requestBody("{\"password\":\"plain-password\"}")
                .startTime(LocalDateTime.of(2026, 7, 13, 12, 0))
                .execUser("TEST")
                .build();
    }

    private String poisonEventId(TestFixture fixture) {
        try {
            Path poisonDirectory = tempDir.resolve("local/XYZ/xyz-test-01/recovery/transaction-db/poison");
            Path poison = Files.list(poisonDirectory).findFirst().orElseThrow();
            return poison.getFileName().toString().replace(".json", "");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private record TestFixture(
            MockEnvironment environment,
            CpfFileLogWriter fileLogWriter,
            TransactionLogFallbackStore store) {
    }
}
