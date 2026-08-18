package com.cpf.batch.control.retention;

import com.cpf.platform.operations.api.retention.CpfRetentionCommand;
import com.cpf.platform.operations.api.retention.CpfRetentionResult;
import com.cpf.platform.operations.spi.retention.CpfRetentionTargetHandler;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

/** BAT 운영 로그의 실제 chunk retention worker. 각 호출은 독립 transaction으로 commit됩니다. */
@Component
public class BatOperationLogRetentionHandler implements CpfRetentionTargetHandler {
    public static final String TARGET = "BAT_OPERATION_LOG";
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public BatOperationLogRetentionHandler(
            @Qualifier("batJdbcTemplate") JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
    }
    @Override public String target() { return TARGET; }

    @Override
    @Transactional(transactionManager = "batTransactionManager")
    public CpfRetentionResult execute(CpfRetentionCommand command) {
        String action = command.policy().action().toUpperCase(Locale.ROOT);
        if (command.policy().legalHold()) {
            return new CpfRetentionResult(TARGET, action, command.policy().dryRun(), true, 0, 0, 0,
                    "LEGAL_HOLD", 0, false, 0);
        }
        if (command.cutoff() == null && !"KEEP".equals(action)) {
            throw new IllegalArgumentException("ARCHIVE/PURGE cutoff은 필수입니다.");
        }
        long matched = command.cutoff() == null ? 0 : jdbc.queryForObject(
                sql.required("retention-operation-log-count"), Long.class, Timestamp.from(command.cutoff()));
        if (command.policy().dryRun() || "KEEP".equals(action)) {
            return new CpfRetentionResult(TARGET, action, true, false, matched, 0, 0,
                    "PREVIEW", 0, false, 0);
        }
        if (!"ARCHIVE".equals(action) && !"PURGE".equals(action)) {
            throw new IllegalArgumentException("지원하지 않는 retention action: " + action);
        }

        List<String> ids = jdbc.query(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT operation_id FROM bat_operation_log WHERE created_at < ? ORDER BY created_at, operation_id");
            ps.setTimestamp(1, Timestamp.from(command.cutoff()));
            ps.setMaxRows(command.maxRows());
            return ps;
        }, (rs, rowNum) -> rs.getString(1));
        if (ids.isEmpty()) {
            return new CpfRetentionResult(TARGET, action, false, false, matched, 0, 0,
                    "SUCCEEDED", 0, false, 0);
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        long archived = 0;
        if ("ARCHIVE".equals(action)) {
            String archiveSql = "INSERT INTO bat_operation_log_archive(" +
                    "operation_id,job_id,execution_id,operation_type,operator_id,reason,before_data,after_data," +
                    "result_type,result_message,created_by,created_at,updated_by,updated_at,archived_at,archived_by,archive_reason) " +
                    "SELECT operation_id,job_id,execution_id,operation_type,operator_id,reason,before_data,after_data," +
                    "result_type,result_message,created_by,created_at,updated_by,updated_at,CURRENT_TIMESTAMP,?,? " +
                    "FROM bat_operation_log WHERE operation_id IN (" + placeholders + ")";
            Object[] args = new Object[2 + ids.size()];
            args[0] = command.actorId(); args[1] = command.reason();
            for (int i = 0; i < ids.size(); i++) args[i + 2] = ids.get(i);
            archived = jdbc.update(archiveSql, args);
        }
        String deleteSql = "DELETE FROM bat_operation_log WHERE operation_id IN (" + placeholders + ")";
        long purged = jdbc.update(deleteSql, ids.toArray());
        long processed = ids.size();
        return new CpfRetentionResult(TARGET, action, false, false, matched, archived, purged,
                "CHUNK_COMMITTED", processed, matched > processed, 0L);
    }
}
