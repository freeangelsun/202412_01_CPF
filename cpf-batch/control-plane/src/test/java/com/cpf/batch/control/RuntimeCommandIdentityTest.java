package com.cpf.batch.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cpf.batch.api.CommandState;
import com.cpf.batch.api.RuntimeCommand;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeCommandIdentityTest {
    @Test
    void canonicalizesTargetsAndAcceptsOracleUppercaseLabels() {
        RuntimeCommand normalized = RuntimeCommandIdentity.normalize(command(
                List.of(" runtime-1 ", "runtime-2"), null, null, Map.of()));

        assertEquals("[\"runtime-1\",\"runtime-2\"]", normalized.targetSnapshot());
        RuntimeCommandIdentity.assertMatches(normalized, persisted(normalized, true));
    }

    @Test
    void rejectsUntrustedSnapshotBeforePersistenceOrDispatch() {
        RuntimeCommandExecutionException failure = assertThrows(
                RuntimeCommandExecutionException.class,
                () -> RuntimeCommandIdentity.normalize(command(
                        List.of("runtime-1"), "[\"runtime-2\"]", "bad", Map.of())));

        assertEquals("BATCH_RUNTIME_COMMAND_TARGET_SNAPSHOT_MISMATCH", failure.code());
        assertEquals(CommandState.FAILED, failure.state());
    }

    @Test
    void rejectsReusedIdempotencyKeyWithChangedPayload() {
        RuntimeCommand normalized = RuntimeCommandIdentity.normalize(command(
                List.of("runtime-1"), null, null, Map.of()));
        Map<String, Object> persisted = persisted(normalized, false);
        persisted.put("target_snapshot", "[\"runtime-2\"]");

        RuntimeCommandExecutionException failure = assertThrows(
                RuntimeCommandExecutionException.class,
                () -> RuntimeCommandIdentity.assertMatches(normalized, persisted));

        assertEquals("BATCH_RUNTIME_COMMAND_IDEMPOTENCY_CONFLICT", failure.code());
        assertEquals(CommandState.FAILED, failure.state());
    }

    @Test
    void rejectsParametersUntilTheyAreDurablyPartOfIdentity() {
        RuntimeCommandExecutionException failure = assertThrows(
                RuntimeCommandExecutionException.class,
                () -> RuntimeCommandIdentity.normalize(command(
                        List.of("runtime-1"), null, null, Map.of("force", true))));

        assertEquals("BATCH_RUNTIME_COMMAND_PARAMETERS_NOT_PERSISTED", failure.code());
    }

    private static RuntimeCommand command(
            List<String> targets, String snapshot, String hash, Map<String, Object> parameters) {
        Instant now = Instant.parse("2026-08-05T03:00:00Z");
        return new RuntimeCommand(
                "CMD-1", "IDEM-1", "restart", "INSTANCE", targets,
                snapshot, hash, 7L, "requester", "approved maintenance",
                now, "POLICY-1", "APR-1", "approver", now.plusSeconds(300),
                CommandState.APPROVED, 0, parameters, null, null, "before", null,
                "OBAT-AA-00000000000000000000000000", null);
    }

    private static Map<String, Object> persisted(RuntimeCommand command, boolean uppercase) {
        Map<String, Object> row = new LinkedHashMap<>();
        put(row, uppercase, "command_id", command.commandId());
        put(row, uppercase, "idempotency_key", command.idempotencyKey());
        put(row, uppercase, "command_type", command.commandType());
        put(row, uppercase, "target_type", command.targetType());
        put(row, uppercase, "target_snapshot", command.targetSnapshot());
        put(row, uppercase, "target_snapshot_hash", command.targetSnapshotHash());
        put(row, uppercase, "expected_version", command.expectedVersion());
        put(row, uppercase, "requested_by", command.requestedBy());
        put(row, uppercase, "reason_text", command.reason());
        put(row, uppercase, "approval_policy_version", command.approvalPolicyVersion());
        put(row, uppercase, "approval_request_id", command.approvalRequestId());
        put(row, uppercase, "approved_by", command.approvedBy());
        put(row, uppercase, "requested_at", Timestamp.from(command.requestedAt()));
        put(row, uppercase, "expires_at", Timestamp.from(command.expiresAt()));
        put(row, uppercase, "transaction_id", command.transactionId());
        return row;
    }

    private static void put(
            Map<String, Object> row, boolean uppercase, String key, Object value) {
        row.put(uppercase ? key.toUpperCase(java.util.Locale.ROOT) : key, value);
    }
}
