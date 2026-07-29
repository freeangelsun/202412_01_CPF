package com.cpf.batch.control.retention;

import com.cpf.core.api.retention.CpfRetentionCommand;
import com.cpf.core.api.retention.CpfRetentionResult;
import com.cpf.core.spi.retention.CpfRetentionTargetHandler;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
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
        if (command.policy().legalHold()) return new CpfRetentionResult(TARGET, action, command.policy().dryRun(), true, 0, 0, 0, "LEGAL_HOLD");
        if (command.cutoff() == null && !"KEEP".equals(action)) throw new IllegalArgumentException("ARCHIVE/PURGE cutoff은 필수입니다.");
        long matched = command.cutoff() == null ? 0 : jdbc.queryForObject(
                sql.required("retention-operation-log-count"),
                Long.class,
                Timestamp.from(command.cutoff()));
        if (command.policy().dryRun() || "KEEP".equals(action)) return new CpfRetentionResult(TARGET, action, true, false, matched, 0, 0, "PREVIEW");
        long archived = 0;
        if ("ARCHIVE".equals(action)) {
            archived = jdbc.update(
                    sql.required("retention-operation-log-archive"),
                    command.actorId(),
                    command.reason(),
                    Timestamp.from(command.cutoff()));
        } else if (!"PURGE".equals(action)) {
            throw new IllegalArgumentException("지원하지 않는 retention action: " + action);
        }
        long purged = jdbc.update(
                sql.required("retention-operation-log-purge"),
                Timestamp.from(command.cutoff()));
        return new CpfRetentionResult(TARGET, action, false, false, matched, archived, purged, "SUCCEEDED");
    }
}
