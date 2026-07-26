package com.cpf.batch.control.retention;

import com.cpf.core.api.retention.CpfRetentionCommand;
import com.cpf.core.api.retention.CpfRetentionResult;
import com.cpf.core.spi.retention.CpfRetentionTargetHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Locale;

/** BAT 운영 로그의 concrete retention handler. */
@Component
public class BatOperationLogRetentionHandler implements CpfRetentionTargetHandler {
    public static final String TARGET = "BAT_OPERATION_LOG";
    private final JdbcTemplate jdbc;
    public BatOperationLogRetentionHandler(@Qualifier("batJdbcTemplate") JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override public String target() { return TARGET; }

    @Override
    @Transactional(transactionManager = "batTransactionManager")
    public CpfRetentionResult execute(CpfRetentionCommand command) {
        String action = command.policy().action().toUpperCase(Locale.ROOT);
        if (command.policy().legalHold()) return new CpfRetentionResult(TARGET, action, command.policy().dryRun(), true, 0, 0, 0, "LEGAL_HOLD");
        if (command.cutoff() == null && !"KEEP".equals(action)) throw new IllegalArgumentException("ARCHIVE/PURGE cutoff은 필수입니다.");
        long matched = command.cutoff() == null ? 0 : jdbc.queryForObject(
                "SELECT COUNT(*) FROM bat_operation_log WHERE created_at < ?", Long.class, Timestamp.from(command.cutoff()));
        if (command.policy().dryRun() || "KEEP".equals(action)) return new CpfRetentionResult(TARGET, action, true, false, matched, 0, 0, "PREVIEW");
        long archived = 0;
        if ("ARCHIVE".equals(action)) {
            archived = jdbc.update("""
                INSERT INTO bat_operation_log_archive (
                    operation_id, job_id, execution_id, operation_type, operator_id, reason,
                    before_data, after_data, result_type, result_message, created_by, created_at,
                    updated_by, updated_at, archived_at, archived_by, archive_reason
                )
                SELECT l.operation_id, l.job_id, l.execution_id, l.operation_type, l.operator_id, l.reason,
                       l.before_data, l.after_data, l.result_type, l.result_message, l.created_by, l.created_at,
                       l.updated_by, l.updated_at, CURRENT_TIMESTAMP(3), ?, ?
                  FROM bat_operation_log l
                 WHERE l.created_at < ?
                """, command.actorId(), command.reason(), Timestamp.from(command.cutoff()));
        } else if (!"PURGE".equals(action)) {
            throw new IllegalArgumentException("지원하지 않는 retention action: " + action);
        }
        long purged = jdbc.update("DELETE FROM bat_operation_log WHERE created_at < ?", Timestamp.from(command.cutoff()));
        return new CpfRetentionResult(TARGET, action, false, false, matched, archived, purged, "SUCCEEDED");
    }
}
