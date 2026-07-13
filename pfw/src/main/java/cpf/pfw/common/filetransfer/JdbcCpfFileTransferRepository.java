package cpf.pfw.common.filetransfer;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * PFW 파일 전송 이력과 중복 방지를 담당하는 JDBC reference adapter입니다.
 *
 * <p>LOCAL/SFTP/FTP/SCP/SSH adapter가 어떤 방식으로 실행되더라도 결과는 동일한 테이블에 남기며,
 * source/destination/checksum/business key 기반 중복 차단과 unknown 결과 후속 조사를 지원합니다.</p>
 */
public class JdbcCpfFileTransferRepository implements CpfFileTransferHistoryPort, CpfDuplicatePreventionPort {
    private final JdbcTemplate jdbcTemplate;

    public JdbcCpfFileTransferRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean alreadyProcessed(String endpointCode, String fileKey, String checksum) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM pfw_file_transfer_history
                WHERE endpoint_code = ?
                  AND duplicate_key = ?
                  AND (? IS NULL OR checksum = ?)
                  AND transfer_status = 'SUCCESS'
                """, Integer.class, endpointCode, fileKey, checksum, checksum);
        return count != null && count > 0;
    }

    @Override
    public void remember(CpfFileTransferRequest request, CpfFileTransferResult result) {
        record(request, result);
    }

    @Override
    public void record(CpfFileTransferRequest request, CpfFileTransferResult result) {
        String duplicateKey = duplicateKey(request);
        try {
            jdbcTemplate.update("""
                    INSERT INTO pfw_file_transfer_history (
                        transfer_id, transaction_global_id, segment_id, endpoint_code, transfer_operation,
                        local_path, remote_path, checksum, file_size, duplicate_key, transfer_status,
                        result_detail, completed_at, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PFW_FILE_TRANSFER', 'PFW_FILE_TRANSFER')
                    """,
                    request.transactionGlobalId() + ":" + request.endpointCode() + ":" + duplicateKey,
                    request.transactionGlobalId(),
                    request.segmentId(),
                    request.endpointCode(),
                    request.operation(),
                    request.localPath(),
                    request.remotePath(),
                    firstText(result.checksum(), request.checksum()),
                    result.fileSize() > 0 ? result.fileSize() : request.fileSize(),
                    duplicateKey,
                    result.status(),
                    result.detail(),
                    Timestamp.from(result.completedAt()));
        } catch (DuplicateKeyException ex) {
            jdbcTemplate.update("""
                    UPDATE pfw_file_transfer_history
                    SET transfer_status = ?,
                        result_detail = ?,
                        completed_at = ?,
                        updated_by = 'PFW_FILE_TRANSFER',
                        updated_at = CURRENT_TIMESTAMP
                    WHERE transfer_id = ?
                    """,
                    result.status(),
                    result.detail(),
                    Timestamp.from(result.completedAt()),
                    request.transactionGlobalId() + ":" + request.endpointCode() + ":" + duplicateKey);
        }
    }

    @Override
    public List<CpfFileTransferResult> findHistory(String endpointCode, Instant from, Instant to, int limit) {
        return jdbcTemplate.queryForList("""
                SELECT transfer_status AS transferStatus,
                       endpoint_code AS endpointCode,
                       local_path AS localPath,
                       remote_path AS remotePath,
                       checksum,
                       file_size AS fileSize,
                       completed_at AS completedAt,
                       result_detail AS resultDetail
                FROM pfw_file_transfer_history
                WHERE (? IS NULL OR endpoint_code = ?)
                  AND (? IS NULL OR created_at >= ?)
                  AND (? IS NULL OR created_at <= ?)
                ORDER BY history_id DESC
                LIMIT ?
                """,
                endpointCode,
                endpointCode,
                timestamp(from),
                timestamp(from),
                timestamp(to),
                timestamp(to),
                safeLimit(limit)).stream().map(this::mapResult).toList();
    }

    public void markUnknown(CpfFileTransferRequest request, String detail) {
        record(request, new CpfFileTransferResult(
                "UNKNOWN",
                request.endpointCode(),
                request.localPath(),
                request.remotePath(),
                request.checksum(),
                request.fileSize(),
                Instant.now(),
                detail));
    }

    private CpfFileTransferResult mapResult(Map<String, Object> row) {
        return new CpfFileTransferResult(
                string(row, "transferStatus"),
                string(row, "endpointCode"),
                string(row, "localPath"),
                string(row, "remotePath"),
                string(row, "checksum"),
                longValue(row.get("fileSize")),
                instant(row, "completedAt"),
                string(row, "resultDetail"));
    }

    private String duplicateKey(CpfFileTransferRequest request) {
        String businessKey = request.attributes().getOrDefault("businessKey", "");
        return String.join("|",
                nullToEmpty(request.endpointCode()),
                nullToEmpty(request.operation()),
                nullToEmpty(request.localPath()),
                nullToEmpty(request.remotePath()),
                nullToEmpty(request.checksum()),
                businessKey);
    }

    private int safeLimit(int limit) {
        return Math.max(1, Math.min(limit, 1000));
    }

    private Timestamp timestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private Instant instant(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        return null;
    }

    private long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? 0L : Long.parseLong(String.valueOf(value));
    }

    private String string(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String firstText(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
