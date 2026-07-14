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
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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

    private TestFixture fixture(Map<String, String> additionalProperties) {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.logging.file.base-path", tempDir.toString())
                .withProperty("cpf.environment", "local")
                .withProperty("cpf.framework.module-id", "ACC")
                .withProperty("cpf.framework.instance-id", "acc-test-01");
        additionalProperties.forEach(environment::setProperty);
        CpfFileLogWriter fileLogWriter = new CpfFileLogWriter(environment);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        TransactionLogFallbackStore store = new TransactionLogFallbackStore(
                objectMapper,
                fileLogWriter,
                environment);
        return new TestFixture(environment, fileLogWriter, store);
    }

    private TransactionLogRecord record() {
        return TransactionLogRecord.builder()
                .transactionId("20260713120000000ACCaccAP010000001")
                .spanId("SEG-0001")
                .sequenceNo(1)
                .businessTransactionId("ACC01TST0001")
                .businessTransactionName("fallback 검증 거래")
                .logType("TRANSACTION")
                .memberNo("MBR-0000123456")
                .requestBody("{\"password\":\"plain-password\"}")
                .startTime(LocalDateTime.of(2026, 7, 13, 12, 0))
                .execUser("TEST")
                .build();
    }

    private record TestFixture(
            MockEnvironment environment,
            CpfFileLogWriter fileLogWriter,
            TransactionLogFallbackStore store) {
    }
}
