package cpf.pfw.common.reconciliation;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PFW unknown result와 reconciliation 테이블을 사용하는 JDBC reference adapter입니다.
 *
 * <p>결과가 불명확한 호출은 이 저장소에 남긴 뒤 자동 재조회, 재시도, 수동 성공/실패 확정으로 이어집니다.
 * 수동 확정은 operatorId와 auditReason을 필수로 받아 운영 감사의 최소 요건을 보장합니다.</p>
 */
public class JdbcCpfReconciliationRepository implements CpfReconciliationPort {
    private final JdbcTemplate jdbcTemplate;

    public JdbcCpfReconciliationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CpfUnknownResultRecord register(CpfUnknownResultRecord record) {
        String unknownId = hasText(record.unknownId()) ? record.unknownId() : "UNK-" + UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO pfw_unknown_result (
                    unknown_id, unknown_type, unknown_status, transaction_global_id, segment_id,
                    external_key, failure_code, failure_message, next_action, detected_at, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PFW_RECONCILIATION', 'PFW_RECONCILIATION')
                """,
                unknownId,
                record.unknownType(),
                record.unknownStatus(),
                record.transactionGlobalId(),
                record.segmentId(),
                record.externalKey(),
                record.failureCode(),
                record.failureMessage(),
                record.nextAction(),
                Timestamp.from(record.detectedAt()));
        return new CpfUnknownResultRecord(
                unknownId,
                record.unknownType(),
                record.unknownStatus(),
                record.transactionGlobalId(),
                record.segmentId(),
                record.externalKey(),
                record.failureCode(),
                record.failureMessage(),
                record.nextAction(),
                record.detectedAt(),
                record.resolvedAt());
    }

    @Override
    public List<CpfUnknownResultRecord> find(String unknownType, String status, int limit) {
        return jdbcTemplate.queryForList("""
                SELECT unknown_id AS unknownId,
                       unknown_type AS unknownType,
                       unknown_status AS unknownStatus,
                       transaction_global_id AS transactionGlobalId,
                       segment_id AS segmentId,
                       external_key AS externalKey,
                       failure_code AS failureCode,
                       failure_message AS failureMessage,
                       next_action AS nextAction,
                       detected_at AS detectedAt,
                       resolved_at AS resolvedAt
                FROM pfw_unknown_result
                WHERE (? IS NULL OR unknown_type = ?)
                  AND (? IS NULL OR unknown_status = ?)
                ORDER BY unknown_seq DESC
                LIMIT ?
                """, unknownType, unknownType, status, status, safeLimit(limit))
                .stream()
                .map(this::mapRecord)
                .toList();
    }

    @Override
    public void resolve(String unknownId, String status, String operatorId, String auditReason) {
        if (!hasText(operatorId)) {
            throw new IllegalArgumentException("operatorId는 필수입니다.");
        }
        if (!hasText(auditReason)) {
            throw new IllegalArgumentException("auditReason은 필수입니다.");
        }
        jdbcTemplate.update("""
                UPDATE pfw_unknown_result
                SET unknown_status = ?,
                    resolved_at = CURRENT_TIMESTAMP(3),
                    resolved_by = ?,
                    audit_reason = ?,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE unknown_id = ?
                """,
                normalize(status),
                operatorId,
                auditReason,
                operatorId,
                unknownId);
    }

    private CpfUnknownResultRecord mapRecord(Map<String, Object> row) {
        return new CpfUnknownResultRecord(
                string(row, "unknownId"),
                string(row, "unknownType"),
                string(row, "unknownStatus"),
                string(row, "transactionGlobalId"),
                string(row, "segmentId"),
                string(row, "externalKey"),
                string(row, "failureCode"),
                string(row, "failureMessage"),
                string(row, "nextAction"),
                instant(row, "detectedAt"),
                instant(row, "resolvedAt"));
    }

    private String normalize(String status) {
        return status == null || status.isBlank() ? "RESOLVED" : status.trim().toUpperCase();
    }

    private int safeLimit(int limit) {
        return Math.max(1, Math.min(limit, 1000));
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

    private String string(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
