package com.cpf.file.sftp;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** Durable SFTP state ledger. STARTED rows are recovered to UNKNOWN after a process-kill lease. */
public class JdbcCpfSftpTransferLedger {
    private final JdbcTemplate jdbc;

    public JdbcCpfSftpTransferLedger(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Transactional(transactionManager = "cpfTransactionManager")
    public void started(CpfSftpTransferRecord record) {
        requireTransaction(record);
        jdbc.update(
                "INSERT INTO cpf_sftp_transfer(transfer_id,operation_code,source_path,target_path,"
                        + "transfer_status,byte_count,transaction_id,detail,started_at,created_at,updated_at) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                record.transferId(), record.operation(), record.sourcePath(), record.targetPath(),
                "STARTED", 0, record.transactionId(), sanitize(record.detail()),
                Timestamp.from(record.startedAt()));
    }

    @Transactional(transactionManager = "cpfTransactionManager")
    public void completed(CpfSftpTransferRecord record) {
        requireTransaction(record);
        int updated = jdbc.update(
                "UPDATE cpf_sftp_transfer SET transfer_status=?,byte_count=?,checksum_value=?,detail=?,"
                        + "completed_at=?,updated_at=CURRENT_TIMESTAMP "
                        + "WHERE transfer_id=? AND transfer_status='STARTED'",
                record.status(), record.bytes(), record.checksum(), sanitize(record.detail()),
                Timestamp.from(record.completedAt()), record.transferId());
        if (updated != 1) {
            throw new IllegalStateException("SFTP transfer ledger conflict: " + record.transferId());
        }
    }

    @Transactional(transactionManager = "cpfTransactionManager")
    public int recoverExpiredStarted(Instant cutoff) {
        Objects.requireNonNull(cutoff, "cutoff");
        return jdbc.update(
                "UPDATE cpf_sftp_transfer SET transfer_status='UNKNOWN',"
                        + "detail='process terminated or lease expired before durable completion',"
                        + "completed_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP "
                        + "WHERE transfer_status='STARTED' AND started_at<?",
                Timestamp.from(cutoff));
    }

    private static void requireTransaction(CpfSftpTransferRecord record) {
        Objects.requireNonNull(record, "record");
        if (record.transactionId() == null || record.transactionId().isBlank()) {
            throw new IllegalArgumentException("SFTP transactionId is required");
        }
    }

    private static String sanitize(String detail) {
        if (detail == null || detail.isBlank()) {
            return detail;
        }
        String value = detail
                .replaceAll("(?i)(password|passwd|pwd|secret|token|api[-_]?key|authorization)\\s*[:=]\\s*[^\\s,;]+", "$1=***")
                .replaceAll("(?i)bearer\\s+[A-Za-z0-9._~+/=-]+", "Bearer ***");
        return value.substring(0, Math.min(1_000, value.length()));
    }
}
