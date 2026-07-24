package com.cpf.core.common.reconciliation;

import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CPF unknown result와 reconciliation 테이블을 사용하는 JDBC reference adapter입니다.
 *
 * <p>결과가 불명확한 호출은 이 저장소에 남긴 뒤 자동 재조회, 재시도, 수동 성공/실패 확정으로 이어집니다.
 * 수동 확정은 operatorId와 auditReason을 필수로 받아 운영 감사의 최소 요건을 보장합니다.</p>
 */
public class JdbcCpfReconciliationRepository implements CpfReconciliationPort {
    private final JdbcTemplate jdbcTemplate;
    private final CpfVendorSqlCatalog sql;

    public JdbcCpfReconciliationRepository(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, new StandardEnvironment());
    }

    public JdbcCpfReconciliationRepository(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.sql = CpfVendorSqlCatalog.create(environment, "cpf");
    }

    @Override
    public CpfUnknownResultRecord register(CpfUnknownResultRecord record) {
        String unknownId = hasText(record.unknownId()) ? record.unknownId() : "UNK-" + UUID.randomUUID();
        jdbcTemplate.update(sql.required("reconciliation-register"),
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
        return jdbcTemplate.queryForList(
                        sql.required("reconciliation-find"),
                        unknownType, unknownType, status, status, safeLimit(limit))
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
        jdbcTemplate.update(sql.required("reconciliation-resolve"),
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
