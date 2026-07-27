package com.cpf.admin.opr.service;

import com.cpf.admin.common.base.AdmBaseService;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.security.CpfSensitiveData;
import com.cpf.core.api.util.CpfStrings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ADM 필수 감사의 durable reservation/relay를 담당합니다.
 *
 * <p>Owner 작업 전에 ADM DB reservation을 {@code REQUIRES_NEW} transaction으로 확정하여 XA 없이도 감사 유실을
 * 방지합니다. Owner 결과 기록이 불명확하면 stale reservation을 {@code UNKNOWN}으로 승격하고 relay가 재처리합니다.</p>
 *
 * <p>Runtime SQL은 MariaDB 전용 시간/페이징 함수에 의존하지 않습니다. 시간, 보존기간, exponential backoff는 Java에서
 * 계산하고 조회 제한은 JDBC {@link PreparedStatement#setMaxRows(int)}로 적용해 MariaDB/PostgreSQL/Oracle 공통 계약을
 * 유지합니다.</p>
 *
 * <p>다중 인스턴스에서는 delivery row를 {@code FOR UPDATE}로 잠근 뒤 immutable audit log를 생성하므로 동일 delivery의
 * 중복 전달을 직렬화합니다.</p>
 */
@Service
public class AdmAuditDeliveryService extends AdmBaseService {
    private static final Logger log = LoggerFactory.getLogger(AdmAuditDeliveryService.class);
    private static final int DEFAULT_MAX_ATTEMPTS = 10;
    private static final int RELAY_BATCH_SIZE = 100;
    private static final int MAX_QUERY_LIMIT = 500;
    private static final int MAX_SNAPSHOT_LENGTH = 16_000;
    private static final int MAX_ERROR_LENGTH = 1_000;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate requiresNew;
    private final int requestedStaleSeconds;

    public AdmAuditDeliveryService(
            @Qualifier("admJdbcTemplate") JdbcTemplate jdbc,
            @Qualifier("admTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${cpf.admin.audit.requested-stale-seconds:900}") int requestedStaleSeconds) {
        this.jdbc = jdbc;
        this.requestedStaleSeconds = Math.max(60, requestedStaleSeconds);
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** 위험 작업 전에 reservation을 확정합니다. 실패하면 Owner 작업을 시작하면 안 됩니다. */
    public long reserve(AuditCommand command) {
        AuditCommand c = command.normalized();
        Timestamp now = nowTimestamp();
        Long id = requiresNew.execute(status -> {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO adm_audit_delivery(
                      TRANSACTION_ID,TRACE_ID,OPERATOR_ID,ACTION_TYPE,TARGET_TYPE,TARGET_ID,REASON,BEFORE_DATA,CLIENT_IP,
                      OPERATION_STATUS,DELIVERY_STATUS,ATTEMPT_COUNT,MAX_ATTEMPTS,NEXT_ATTEMPT_AT,CREATED_BY,UPDATED_BY)
                    VALUES(?,?,?,?,?,?,?,?,?,'REQUESTED','PENDING',0,?,?,?,?)
                    """, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, c.transactionId());
                ps.setString(2, c.traceId());
                ps.setString(3, c.operatorId());
                ps.setString(4, c.actionType());
                ps.setString(5, c.targetType());
                ps.setString(6, c.targetId());
                ps.setString(7, c.reason());
                ps.setString(8, sanitizeSnapshot(c.beforeData()));
                ps.setString(9, c.clientIp());
                ps.setInt(10, DEFAULT_MAX_ATTEMPTS);
                ps.setTimestamp(11, now);
                ps.setString(12, c.operatorId());
                ps.setString(13, c.operatorId());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new IllegalStateException("ADM 감사 reservation ID를 발급받지 못했습니다.");
            }
            return key.longValue();
        });
        if (id == null) {
            throw new IllegalStateException("ADM 감사 reservation을 저장하지 못했습니다.");
        }
        return id;
    }

    public void enrichReservation(long id, AuditCommand command) {
        AuditCommand c = command.normalized();
        requiresNew.executeWithoutResult(status -> {
            int updated = jdbc.update("""
                UPDATE adm_audit_delivery
                   SET TRANSACTION_ID=?,TRACE_ID=?,OPERATOR_ID=?,ACTION_TYPE=?,TARGET_TYPE=?,TARGET_ID=?,
                       REASON=?,BEFORE_DATA=?,CLIENT_IP=?,UPDATED_BY=?,UPDATED_AT=?
                 WHERE DELIVERY_ID=? AND DELIVERY_STATUS<>'DELIVERED'
                """,
                c.transactionId(), c.traceId(), c.operatorId(), c.actionType(), c.targetType(), c.targetId(),
                c.reason(), sanitizeSnapshot(c.beforeData()), c.clientIp(), c.operatorId(), nowTimestamp(), id);
            if (updated != 1) {
                throw new IllegalStateException("ADM 감사 reservation 보강 충돌입니다. deliveryId=" + id);
            }
        });
    }

    public <T> T executeAudited(AuditCommand command, Supplier<T> operation, Function<T, String> afterMapper) {
        long id = reserve(command);
        try {
            T result = operation.get();
            completeOperation(id, "SUCCEEDED", afterMapper == null ? null : afterMapper.apply(result), null);
            return result;
        } catch (RuntimeException ex) {
            completeOperation(id, "FAILED", null, "OWNER_OPERATION_FAILED: " + ex.getClass().getSimpleName());
            throw ex;
        }
    }

    public void record(AuditCommand command, String after, String diff) {
        long id = reserve(command);
        completeOperation(id, "SUCCEEDED", after, diff);
    }

    /** 결과 기록 실패 시 reservation은 REQUESTED로 남고 stale recovery가 UNKNOWN으로 승격합니다. */
    public void completeOperation(long id, String operationStatus, String after, String diff) {
        try {
            requiresNew.executeWithoutResult(status -> {
                int updated = jdbc.update("""
                    UPDATE adm_audit_delivery
                       SET OPERATION_STATUS=?,AFTER_DATA=?,DIFF_DATA=?,UPDATED_BY=OPERATOR_ID,UPDATED_AT=?
                     WHERE DELIVERY_ID=? AND DELIVERY_STATUS<>'DELIVERED'
                    """, operationStatus, sanitizeSnapshot(after), sanitizeSnapshot(diff), nowTimestamp(), id);
                if (updated != 1) {
                    throw new IllegalStateException("ADM 감사 reservation 상태 갱신 충돌입니다. deliveryId=" + id);
                }
            });
        } catch (RuntimeException ex) {
            log.error("ADM 감사 결과 기록 실패. deliveryId={}, transactionId={}, reason={}",
                    id, safeTransactionId(), safeMessage(ex));
            return;
        }
        deliverNow(id, false);
    }

    public List<Map<String, Object>> findDeliveries(String state, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_QUERY_LIMIT));
        String sql = CpfStrings.hasText(state)
                ? """
                  SELECT DELIVERY_ID,TRANSACTION_ID,TRACE_ID,OPERATOR_ID,ACTION_TYPE,TARGET_TYPE,TARGET_ID,REASON,
                         OPERATION_STATUS,DELIVERY_STATUS,ATTEMPT_COUNT,MAX_ATTEMPTS,NEXT_ATTEMPT_AT,LAST_ERROR,AUDIT_ID,REQUESTED_AT,DELIVERED_AT,UPDATED_AT
                    FROM adm_audit_delivery
                   WHERE DELIVERY_STATUS=?
                   ORDER BY DELIVERY_ID DESC
                  """
                : """
                  SELECT DELIVERY_ID,TRANSACTION_ID,TRACE_ID,OPERATOR_ID,ACTION_TYPE,TARGET_TYPE,TARGET_ID,REASON,
                         OPERATION_STATUS,DELIVERY_STATUS,ATTEMPT_COUNT,MAX_ATTEMPTS,NEXT_ATTEMPT_AT,LAST_ERROR,AUDIT_ID,REQUESTED_AT,DELIVERED_AT,UPDATED_AT
                    FROM adm_audit_delivery
                   ORDER BY DELIVERY_ID DESC
                  """;
        return jdbc.query(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            if (CpfStrings.hasText(state)) {
                ps.setString(1, state.trim().toUpperCase());
            }
            ps.setMaxRows(safeLimit);
            return ps;
        }, new ColumnMapRowMapper());
    }

    public Map<String, Object> findDelivery(long id) {
        return jdbc.queryForMap("SELECT * FROM adm_audit_delivery WHERE DELIVERY_ID=?", id);
    }

    public Map<String, Object> retry(long id, String operatorId, String reason) {
        String actor = require(operatorId, "operatorId");
        String why = CpfSensitiveData.sanitizeAuditReason(reason);
        Timestamp now = nowTimestamp();
        requiresNew.executeWithoutResult(status -> {
            int updated = jdbc.update("""
                UPDATE adm_audit_delivery
                   SET DELIVERY_STATUS='RETRY',NEXT_ATTEMPT_AT=?,LAST_ERROR=?,UPDATED_BY=?,UPDATED_AT=?
                 WHERE DELIVERY_ID=? AND DELIVERY_STATUS IN('PENDING','RETRY','FAILED')
                """, now, truncate("manual retry: " + why, MAX_ERROR_LENGTH), actor, now, id);
            if (updated != 1) {
                throw new IllegalStateException("재처리 가능한 감사 전달 건이 아닙니다. deliveryId=" + id);
            }
        });
        deliverNow(id, true);
        return findDelivery(id);
    }

    @Scheduled(fixedDelayString = "${cpf.admin.audit.relay-delay-ms:5000}")
    public void relayPending() {
        recoverStaleRequested();
        try {
            List<Long> ids = jdbc.query(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                    SELECT DELIVERY_ID
                      FROM adm_audit_delivery
                     WHERE DELIVERY_STATUS IN('PENDING','RETRY')
                       AND OPERATION_STATUS IN('SUCCEEDED','FAILED','UNKNOWN')
                       AND (NEXT_ATTEMPT_AT IS NULL OR NEXT_ATTEMPT_AT<=?)
                     ORDER BY DELIVERY_ID
                    """);
                ps.setTimestamp(1, nowTimestamp());
                ps.setMaxRows(RELAY_BATCH_SIZE);
                return ps;
            }, (rs, rowNum) -> rs.getLong(1));
            ids.forEach(id -> deliverNow(id, false));
        } catch (DataAccessException ex) {
            log.error("ADM 감사 relay 대상 조회 실패. transactionId={}, reason={}", safeTransactionId(), safeMessage(ex));
        }
    }

    private void recoverStaleRequested() {
        Timestamp now = nowTimestamp();
        Timestamp staleCutoff = Timestamp.from(now.toInstant().minusSeconds(requestedStaleSeconds));
        try {
            requiresNew.executeWithoutResult(status -> jdbc.update("""
                UPDATE adm_audit_delivery
                   SET OPERATION_STATUS='UNKNOWN',DELIVERY_STATUS='RETRY',NEXT_ATTEMPT_AT=?,
                       LAST_ERROR=COALESCE(LAST_ERROR,'stale REQUESTED recovered as UNKNOWN'),UPDATED_AT=?
                 WHERE OPERATION_STATUS='REQUESTED'
                   AND DELIVERY_STATUS IN('PENDING','RETRY')
                   AND REQUESTED_AT<=?
                """, now, now, staleCutoff));
        } catch (RuntimeException ex) {
            log.error("ADM stale audit reservation 복구 실패. transactionId={}, reason={}",
                    safeTransactionId(), safeMessage(ex));
        }
    }

    /** FOR UPDATE를 사용해 다중 ADM 인스턴스 relay의 중복 전달을 직렬화합니다. */
    private void deliverNow(long id, boolean manual) {
        try {
            requiresNew.executeWithoutResult(status -> {
                Map<String, Object> row = jdbc.queryForMap("""
                    SELECT DELIVERY_ID,TRANSACTION_ID,TRACE_ID,OPERATOR_ID,ACTION_TYPE,TARGET_TYPE,TARGET_ID,REASON,
                           BEFORE_DATA,AFTER_DATA,DIFF_DATA,CLIENT_IP,OPERATION_STATUS,DELIVERY_STATUS,ATTEMPT_COUNT,MAX_ATTEMPTS
                      FROM adm_audit_delivery
                     WHERE DELIVERY_ID=?
                     FOR UPDATE
                    """, id);
                String deliveryStatus = text(row.get("DELIVERY_STATUS"));
                if ("DELIVERED".equals(deliveryStatus)) {
                    return;
                }
                String operationStatus = text(row.get("OPERATION_STATUS"));
                if ("REQUESTED".equals(operationStatus)) {
                    return;
                }
                int attempts = number(row.get("ATTEMPT_COUNT"));
                int maxAttempts = Math.max(1, number(row.get("MAX_ATTEMPTS")));
                if (attempts >= maxAttempts && !manual) {
                    jdbc.update("""
                        UPDATE adm_audit_delivery
                           SET DELIVERY_STATUS='FAILED',NEXT_ATTEMPT_AT=NULL,UPDATED_AT=?
                         WHERE DELIVERY_ID=?
                        """, nowTimestamp(), id);
                    return;
                }

                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbc.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement("""
                        INSERT INTO adm_audit_log(
                          TRANSACTION_ID,TRACE_ID,OPERATOR_ID,ACTION_TYPE,TARGET_TYPE,TARGET_ID,REASON,
                          BEFORE_DATA,AFTER_DATA,DIFF_DATA,CLIENT_IP,RETENTION_UNTIL,IMMUTABLE_YN,CREATED_BY,UPDATED_BY)
                        VALUES(?,?,?,?,?,?,?,?,?,?,?,?, 'Y',?,?)
                        """, Statement.RETURN_GENERATED_KEYS);
                    String operatorId = text(row.get("OPERATOR_ID"));
                    ps.setString(1, text(row.get("TRANSACTION_ID")));
                    ps.setString(2, text(row.get("TRACE_ID")));
                    ps.setString(3, operatorId);
                    ps.setString(4, text(row.get("ACTION_TYPE")));
                    ps.setString(5, text(row.get("TARGET_TYPE")));
                    ps.setString(6, text(row.get("TARGET_ID")));
                    ps.setString(7, CpfSensitiveData.sanitizeAuditText(text(row.get("REASON"))));
                    ps.setString(8, sanitizeSnapshot(text(row.get("BEFORE_DATA"))));
                    ps.setString(9, sanitizeSnapshot(text(row.get("AFTER_DATA"))));
                    String diff = sanitizeSnapshot(text(row.get("DIFF_DATA")));
                    String suffix = "operationStatus=" + operationStatus + ";deliveryId=" + id;
                    ps.setString(10, truncate(CpfStrings.hasText(diff) ? diff + "\n" + suffix : suffix, MAX_SNAPSHOT_LENGTH));
                    ps.setString(11, text(row.get("CLIENT_IP")));
                    ps.setDate(12, Date.valueOf(LocalDate.now().plusYears(5)));
                    ps.setString(13, operatorId);
                    ps.setString(14, operatorId);
                    return ps;
                }, keyHolder);
                Number auditId = keyHolder.getKey();
                if (auditId == null) {
                    throw new IllegalStateException("immutable audit ID 발급 실패. deliveryId=" + id);
                }
                Timestamp deliveredAt = nowTimestamp();
                jdbc.update("""
                    UPDATE adm_audit_delivery
                       SET DELIVERY_STATUS='DELIVERED',ATTEMPT_COUNT=ATTEMPT_COUNT+1,AUDIT_ID=?,
                           DELIVERED_AT=?,NEXT_ATTEMPT_AT=NULL,LAST_ERROR=NULL,UPDATED_AT=?
                     WHERE DELIVERY_ID=?
                    """, auditId.longValue(), deliveredAt, deliveredAt, id);
            });
        } catch (RuntimeException ex) {
            markRetry(id, ex);
        }
    }

    private void markRetry(long id, RuntimeException cause) {
        try {
            requiresNew.executeWithoutResult(status -> {
                Map<String, Object> row = jdbc.queryForMap("""
                    SELECT ATTEMPT_COUNT,MAX_ATTEMPTS
                      FROM adm_audit_delivery
                     WHERE DELIVERY_ID=? AND DELIVERY_STATUS<>'DELIVERED'
                     FOR UPDATE
                    """, id);
                int nextAttempt = number(row.get("ATTEMPT_COUNT")) + 1;
                int maxAttempts = Math.max(1, number(row.get("MAX_ATTEMPTS")));
                boolean failed = nextAttempt >= maxAttempts;
                long backoffSeconds = Math.min(300L, 1L << Math.min(nextAttempt, 8));
                Timestamp now = nowTimestamp();
                Timestamp nextAt = failed ? null : Timestamp.from(now.toInstant().plusSeconds(backoffSeconds));
                jdbc.update("""
                    UPDATE adm_audit_delivery
                       SET ATTEMPT_COUNT=?,DELIVERY_STATUS=?,NEXT_ATTEMPT_AT=?,LAST_ERROR=?,UPDATED_AT=?
                     WHERE DELIVERY_ID=? AND DELIVERY_STATUS<>'DELIVERED'
                    """, nextAttempt, failed ? "FAILED" : "RETRY", nextAt,
                        truncate(safeMessage(cause), MAX_ERROR_LENGTH), now, id);
            });
        } catch (RuntimeException ex) {
            log.error("ADM 감사 retry 상태 기록 실패. deliveryId={}, transactionId={}, reason={}",
                    id, safeTransactionId(), safeMessage(ex));
        }
    }

    private String safeTransactionId() {
        try {
            return CpfTransactionContext.transactionId();
        } catch (RuntimeException ex) {
            return "UNAVAILABLE";
        }
    }

    private static Timestamp nowTimestamp() {
        return Timestamp.from(Instant.now());
    }

    private static String sanitizeSnapshot(String value) {
        return truncate(CpfSensitiveData.sanitizeAuditText(value), MAX_SNAPSHOT_LENGTH);
    }

    private static String safeMessage(Throwable ex) {
        String raw = ex == null
                ? "unknown"
                : (ex.getMessage() == null || ex.getMessage().isBlank() ? ex.getClass().getSimpleName() : ex.getMessage());
        return truncate(CpfSensitiveData.sanitizeAuditText(raw), MAX_ERROR_LENGTH);
    }

    private static String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }

    private static String require(String value, String field) {
        if (!CpfStrings.hasText(value)) {
            throw new IllegalArgumentException(field + "은(는) 필수입니다.");
        }
        return value.trim();
    }

    private static int number(Object value) {
        return value instanceof Number number
                ? number.intValue()
                : (value == null ? 0 : Integer.parseInt(String.valueOf(value)));
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public record AuditCommand(
            String transactionId,
            String traceId,
            String operatorId,
            String actionType,
            String targetType,
            String targetId,
            String reason,
            String beforeData,
            String clientIp) {

        AuditCommand normalized() {
            return new AuditCommand(
                    CpfStrings.hasText(transactionId) ? transactionId.trim() : CpfTransactionContext.transactionId(),
                    CpfStrings.hasText(traceId) ? traceId.trim() : CpfTransactionContext.traceId(),
                    require(operatorId, "operatorId"),
                    require(actionType, "actionType"),
                    targetType == null ? null : targetType.trim(),
                    targetId == null ? null : targetId.trim(),
                    CpfSensitiveData.sanitizeAuditReason(reason),
                    beforeData,
                    clientIp == null ? null : clientIp.trim());
        }
    }
}
