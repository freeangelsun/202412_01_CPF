package com.cpf.reference.edu.runtime.persistence;

import com.cpf.reference.edu.runtime.application.EduConflictException;
import com.cpf.reference.edu.runtime.model.EduExecutionState;
import com.cpf.reference.edu.runtime.model.EduFailurePoint;
import com.cpf.reference.edu.runtime.model.EduOperationRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Opt-in live DB contract. The official PowerShell runner supplies connection secrets only to the
 * forked test process and requires a sanitized sentinel artifact before it records PASS.
 */
class JdbcEduOperationRepositoryLiveIdempotencyTest {
    @Test
    void sameKeySameHashReplaysButDifferentHashConflictsAndKeepsOneRow() throws Exception {
        Assumptions.assumeTrue("true".equalsIgnoreCase(System.getenv("CPF_REF_LIVE_DB_TEST")));
        String jdbcUrl = required("CPF_REF_LIVE_JDBC_URL");
        String username = required("CPF_REF_LIVE_DB_USERNAME");
        String password = required("CPF_REF_LIVE_DB_PASSWORD");
        Path resultPath = Path.of(required("CPF_REF_LIVE_RESULT_PATH")).toAbsolutePath().normalize();

        DriverManagerDataSource dataSource = new DriverManagerDataSource(jdbcUrl, username, password);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        JdbcEduOperationRepository repository = new JdbcEduOperationRepository(jdbc, new ObjectMapper());
        String suffix = UUID.randomUUID().toString();
        String requirementId = "EDU-DEV-05";
        String idempotencyKey = "live-hash-" + suffix;
        String firstOperationId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        EduOperationRecord first = record(firstOperationId, requirementId, idempotencyKey,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Map.of("amount", "1000.00"), now);
        EduOperationRecord sameHash = record(UUID.randomUUID().toString(), requirementId, idempotencyKey,
                first.payloadHash(), Map.of("amount", "1000.00"), now.plusMillis(1));
        EduOperationRecord differentHash = record(UUID.randomUUID().toString(), requirementId, idempotencyKey,
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", Map.of("amount", "2000.00"), now.plusMillis(2));

        boolean replayed = false;
        boolean conflict = false;
        int rowCount = -1;
        try {
            assertTrue(!repository.create(first).duplicate(), "first insert must create one row");
            var replay = repository.create(sameHash);
            replayed = replay.duplicate();
            assertTrue(replayed, "same key/same payload hash must replay");
            assertEquals(firstOperationId, replay.operation().operationId());

            assertThrows(EduConflictException.class, () -> repository.create(differentHash));
            conflict = true;
            rowCount = jdbc.queryForObject(
                    "select count(*) from CPF_EDU_OPERATION where REQUIREMENT_ID=? and IDEMPOTENCY_KEY=?",
                    Integer.class, requirementId, idempotencyKey);
            assertEquals(1, rowCount);
            assertEquals(first.payloadHash(), jdbc.queryForObject(
                    "select PAYLOAD_HASH from CPF_EDU_OPERATION where REQUIREMENT_ID=? and IDEMPOTENCY_KEY=?",
                    String.class, requirementId, idempotencyKey));
        } finally {
            jdbc.update("delete from CPF_EDU_OPERATION where REQUIREMENT_ID=? and IDEMPOTENCY_KEY=?",
                    requirementId, idempotencyKey);
        }

        Files.createDirectories(resultPath.getParent());
        new ObjectMapper().writeValue(resultPath.toFile(), Map.of(
                "status", "PASS",
                "sameHashReplay", replayed,
                "differentHashConflict", conflict,
                "rowCountBeforeCleanup", rowCount,
                "cleanupRowCount", jdbc.queryForObject(
                        "select count(*) from CPF_EDU_OPERATION where REQUIREMENT_ID=? and IDEMPOTENCY_KEY=?",
                        Integer.class, requirementId, idempotencyKey)));
    }

    private static EduOperationRecord record(
            String operationId,
            String requirementId,
            String idempotencyKey,
            String payloadHash,
            Map<String, Object> payload,
            Instant now) {
        return new EduOperationRecord(
                operationId, requirementId, "live-business-key", idempotencyKey, payloadHash,
                "cpf-live-test", "ROLE_CPF_TEST", "CPF_LIVE_TEST", EduExecutionState.REQUESTED,
                0L, 0L, 0L, 0, 3, EduFailurePoint.NONE, "", "",
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), payload, Map.of(),
                now, now, null);
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required live DB environment variable is missing: " + name);
        }
        return value;
    }
}
