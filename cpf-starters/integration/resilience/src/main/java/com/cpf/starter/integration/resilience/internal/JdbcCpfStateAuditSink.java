package com.cpf.starter.integration.resilience.internal;

import com.cpf.core.api.state.CpfStateAuditEvent;
import com.cpf.core.spi.state.CpfStateAuditSink;
import java.sql.Timestamp;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;

/** JDBC audit sink that persists only hashed state/operation identifiers and sanitized reasons. */
final class JdbcCpfStateAuditSink implements CpfStateAuditSink {
    private final JdbcTemplate jdbc;

    JdbcCpfStateAuditSink(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public void record(CpfStateAuditEvent event) {
        Objects.requireNonNull(event, "event");
        int inserted = jdbc.update("""
                INSERT INTO cpf_state_audit
                       (state_key_hash, operation_id_hash, actor_id, before_state,
                        requested_state, resulting_state, before_version, resulting_version,
                        decision_code, audit_reason, decided_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, event.stateKeyHash(), event.operationIdHash(), event.actor(),
                event.beforeState() == null ? null : event.beforeState().name(),
                event.requestedState().name(),
                event.resultingState() == null ? null : event.resultingState().name(),
                event.beforeVersion(), event.resultingVersion(), event.decision(), event.reason(),
                Timestamp.from(event.decidedAt()));
        if (inserted != 1) {
            throw new IllegalStateException("state audit insert affected " + inserted + " rows");
        }
    }
}
