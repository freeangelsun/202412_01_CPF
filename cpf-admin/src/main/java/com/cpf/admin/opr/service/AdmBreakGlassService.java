package com.cpf.admin.opr.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * ADM break-glass 세션 통제 서비스.
 *
 * <p>Break-glass는 승인 자체를 우회하는 전역 스위치가 아닙니다. 좁은 scope와 짧은 TTL을 가진
 * 세션을 발급하고, 실제 Owner Command가 명시적으로 scope를 소비할 때만 사용할 수 있습니다.</p>
 */
@Service
public class AdmBreakGlassService extends com.cpf.admin.common.base.AdmBaseService {
    private final JdbcTemplate jdbc;
    private final int maxTtlMinutes;

    public AdmBreakGlassService(@Qualifier("admJdbcTemplate") JdbcTemplate jdbc,
                                @Value("${cpf.adm.break-glass.max-ttl-minutes:30}") int maxTtlMinutes) {
        this.jdbc = jdbc;
        this.maxTtlMinutes = Math.max(1, Math.min(maxTtlMinutes, 120));
    }

    public List<Map<String,Object>> list(String status, int limit) {
        expireStale();
        int bounded = Math.max(1, Math.min(limit <= 0 ? 100 : limit, 500));
        if (status == null || status.isBlank()) {
            return AdmJdbcQueries.queryForList(
                    jdbc,
                    """
                    SELECT session_id AS sessionId, operator_id AS operatorId, scope_type AS scopeType,
                           scope_value AS scopeValue, reason, status, expires_at AS expiresAt,
                           closed_at AS closedAt, post_review_status AS postReviewStatus,
                           reviewed_by AS reviewedBy, review_reason AS reviewReason, created_at AS createdAt
                    FROM adm_break_glass_session
                    ORDER BY created_at DESC
                    """,
                    List.of(),
                    bounded);
        }
        return AdmJdbcQueries.queryForList(
                jdbc,
                """
                SELECT session_id AS sessionId, operator_id AS operatorId, scope_type AS scopeType,
                       scope_value AS scopeValue, reason, status, expires_at AS expiresAt,
                       closed_at AS closedAt, post_review_status AS postReviewStatus,
                       reviewed_by AS reviewedBy, review_reason AS reviewReason, created_at AS createdAt
                FROM adm_break_glass_session
                WHERE status = ?
                ORDER BY created_at DESC
                """,
                List.of(status.trim().toUpperCase(Locale.ROOT)),
                bounded);
    }

    @Transactional(transactionManager = "admTransactionManager")
    public Map<String,Object> open(String operatorId, String scopeType, String scopeValue, String reason, int ttlMinutes) {
        String operator = required(operatorId, "operatorId");
        String type = required(scopeType, "scopeType").toUpperCase(Locale.ROOT);
        String value = required(scopeValue, "scopeValue");
        String why = required(reason, "reason");
        int ttl = ttlMinutes <= 0 ? 15 : ttlMinutes;
        if (ttl > maxTtlMinutes) throw new IllegalArgumentException("break-glass TTL은 " + maxTtlMinutes + "분을 초과할 수 없습니다.");
        Long active = jdbc.queryForObject("SELECT COUNT(*) FROM adm_break_glass_session WHERE operator_id=? AND status='ACTIVE' AND expires_at>CURRENT_TIMESTAMP(3)", Long.class, operator);
        if (active != null && active > 0) throw new IllegalStateException("운영자별 ACTIVE break-glass 세션은 하나만 허용합니다.");
        String id = UUID.randomUUID().toString();
        Instant expires = Instant.now().plus(ttl, ChronoUnit.MINUTES);
        jdbc.update("INSERT INTO adm_break_glass_session(session_id,operator_id,scope_type,scope_value,reason,status,expires_at,post_review_status,created_by,updated_by) VALUES(?,?,?,?,?,'ACTIVE',?,'PENDING',?,?)", id, operator, type, value, why, java.sql.Timestamp.from(expires), operator, operator);
        return get(id);
    }

    @Transactional(transactionManager = "admTransactionManager")
    public Map<String,Object> close(String sessionId, String operatorId, String reason) {
        String id = required(sessionId, "sessionId");
        String operator = required(operatorId, "operatorId");
        int n = jdbc.update("UPDATE adm_break_glass_session SET status='CLOSED', closed_at=CURRENT_TIMESTAMP(3), close_reason=?, updated_by=? WHERE session_id=? AND operator_id=? AND status='ACTIVE'", required(reason, "reason"), operator, id, operator);
        if (n != 1) throw new IllegalStateException("본인의 ACTIVE break-glass 세션만 종료할 수 있습니다.");
        return get(id);
    }

    @Transactional(transactionManager = "admTransactionManager")
    public Map<String,Object> review(String sessionId, String reviewerId, String reviewStatus, String reason) {
        String status = required(reviewStatus, "reviewStatus").toUpperCase(Locale.ROOT);
        if (!status.equals("APPROVED") && !status.equals("REJECTED")) throw new IllegalArgumentException("사후검토 상태는 APPROVED/REJECTED만 허용합니다.");
        int n = jdbc.update("UPDATE adm_break_glass_session SET post_review_status=?, reviewed_by=?, reviewed_at=CURRENT_TIMESTAMP(3), review_reason=?, updated_by=? WHERE session_id=? AND status IN ('CLOSED','EXPIRED')", status, required(reviewerId, "reviewerId"), required(reason, "reason"), reviewerId, required(sessionId, "sessionId"));
        if (n != 1) throw new IllegalStateException("종료/만료된 break-glass 세션만 사후검토할 수 있습니다.");
        return get(sessionId);
    }

    public boolean isActive(String operatorId, String scopeType, String scopeValue) {
        expireStale();
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM adm_break_glass_session WHERE operator_id=? AND scope_type=? AND scope_value=? AND status='ACTIVE' AND expires_at>CURRENT_TIMESTAMP(3)", Long.class, required(operatorId,"operatorId"), required(scopeType,"scopeType").toUpperCase(Locale.ROOT), required(scopeValue,"scopeValue"));
        return count != null && count > 0;
    }

    private Map<String,Object> get(String sessionId) {
        return jdbc.queryForMap("SELECT session_id AS sessionId, operator_id AS operatorId, scope_type AS scopeType, scope_value AS scopeValue, reason, status, expires_at AS expiresAt, closed_at AS closedAt, close_reason AS closeReason, post_review_status AS postReviewStatus, reviewed_by AS reviewedBy, reviewed_at AS reviewedAt, review_reason AS reviewReason, created_at AS createdAt FROM adm_break_glass_session WHERE session_id=?", sessionId);
    }

    private void expireStale() {
        jdbc.update("UPDATE adm_break_glass_session SET status='EXPIRED', closed_at=COALESCE(closed_at,CURRENT_TIMESTAMP(3)), close_reason=COALESCE(close_reason,'TTL_EXPIRED'), updated_by='SYSTEM' WHERE status='ACTIVE' AND expires_at<=CURRENT_TIMESTAMP(3)");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
}
