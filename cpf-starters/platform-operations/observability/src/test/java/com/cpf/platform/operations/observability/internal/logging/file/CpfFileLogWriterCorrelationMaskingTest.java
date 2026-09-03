package com.cpf.platform.operations.observability.internal.logging.file;

import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** File Log masking이 개인정보를 보호하면서 CPF 거래 lineage 키를 훼손하지 않는지 검증합니다. */
class CpfFileLogWriterCorrelationMaskingTest {

    @Test
    void preservesInstanceAndTransactionLineageWhileMaskingSensitiveKeyValues(@TempDir Path logRoot)
            throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.environment", "local")
                .withProperty("cpf.framework.module-id", "MBR")
                .withProperty("cpf.logging.file.base-path", logRoot.toAbsolutePath().toString());
        String instanceId = "bat-domain-20260903130724494";
        String transactionId = "20260903040840408BATANDN63S0000001";
        String segmentId = transactionId + "-SEG-0002-AC3BB146";

        try (CpfFileLogWriter writer = new CpfFileLogWriter(environment)) {
            writer.writeEventAtRelativePath(Path.of("correlation.log"), Map.of(
                    "instanceId", instanceId,
                    "transactionId", transactionId,
                    "traceId", "66ea9962f7144bbc20d624244928192b",
                    "segmentId", segmentId,
                    "accountNo", "12345678901234567"));
        }

        String line = Files.readString(logRoot.resolve("local").resolve("correlation.log"));
        assertTrue(line.contains(instanceId), line);
        assertTrue(line.contains(transactionId), line);
        assertTrue(line.contains(segmentId), line);
        assertFalse(line.contains("12345678901234567"), line);
        assertTrue(line.contains("\"accountNo\":\"***\""), line);
    }

    @Test
    void writesCanonicalResultCodesWithTheSameNamesAsTheDbSummary(@TempDir Path logRoot)
            throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.environment", "local")
                .withProperty("cpf.framework.module-id", "MBR")
                .withProperty("cpf.logging.file.base-path", logRoot.toAbsolutePath().toString());
        TransactionLogRecord record = TransactionLogRecord.builder()
                .moduleId("MBR")
                .transactionId("20260903040840408BATANDN63S0000001")
                .traceId("66ea9962f7144bbc20d624244928192b")
                .standardExecutionId("MBR_SAMPLE_TX_CREATE")
                .logType("SUCCESS")
                .responseCode("SMEM000001")
                .messageCode("MMEM000001")
                .errorCode(null)
                .startTime(LocalDateTime.of(2026, 9, 3, 12, 0))
                .build();
        
        try (CpfFileLogWriter writer = new CpfFileLogWriter(environment)) {
            writer.writeTransaction(record, Map.of(), null);
        }

        String line;
        try (var paths = Files.walk(logRoot)) {
            line = paths.filter(Files::isRegularFile)
                    .map(path -> {
                        try {
                            return Files.readString(path);
                        } catch (java.io.IOException exception) {
                            throw new java.io.UncheckedIOException(exception);
                        }
                    })
                    .collect(Collectors.joining("\n"));
        }
        assertTrue(line.contains("\"responseCode\":\"SMEM000001\""), line);
        assertTrue(line.contains("\"messageCode\":\"MMEM000001\""), line);
        assertTrue(line.contains("\"errorCode\":null"), line);
    }
}
