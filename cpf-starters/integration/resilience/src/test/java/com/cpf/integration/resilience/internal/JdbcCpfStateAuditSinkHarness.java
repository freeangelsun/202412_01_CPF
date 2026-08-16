package com.cpf.integration.resilience.internal;

import com.cpf.platform.operations.api.state.CpfOperationState;
import com.cpf.platform.operations.api.state.CpfStateAuditEvent;
import java.time.Instant;
import java.util.Arrays;
import org.springframework.jdbc.core.JdbcTemplate;

public final class JdbcCpfStateAuditSinkHarness {
    private JdbcCpfStateAuditSinkHarness() {}

    public static void main(String[] args) {
        RecordingJdbc jdbc = new RecordingJdbc();
        JdbcCpfStateAuditSink sink = new JdbcCpfStateAuditSink(jdbc);
        sink.record(event());
        require(jdbc.calls == 1, "audit insert must execute once");
        require(Arrays.stream(jdbc.arguments).noneMatch(value -> "raw-state-key".equals(value)),
                "raw state key must not be persisted");
        require(Arrays.stream(jdbc.arguments).noneMatch(value -> "raw-operation-id".equals(value)),
                "raw operation id must not be persisted");

        jdbc.affectedRows = 0;
        try {
            sink.record(event());
            throw new AssertionError("zero-row audit insert must fail closed");
        } catch (IllegalStateException expected) {
            // expected
        }
        System.out.println("CPF_JDBC_STATE_AUDIT_HARNESS_PASS");
    }

    private static CpfStateAuditEvent event() {
        return new CpfStateAuditEvent(
                "a".repeat(64), "b".repeat(64), "worker-a",
                CpfOperationState.RUNNING, CpfOperationState.SUCCEEDED,
                CpfOperationState.SUCCEEDED, 0L, 1L, "APPLIED", "confirmed",
                Instant.parse("2026-08-05T00:00:00Z"));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class RecordingJdbc extends JdbcTemplate {
        private int calls;
        private int affectedRows = 1;
        private Object[] arguments = new Object[0];

        @Override
        public int update(String sql, Object... args) {
            calls++;
            arguments = args.clone();
            return affectedRows;
        }
    }
}
