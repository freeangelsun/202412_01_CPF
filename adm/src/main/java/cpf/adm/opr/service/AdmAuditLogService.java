package cpf.adm.opr.service;

import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.CpfValidationException;
import cpf.pfw.common.logging.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ADM 운영 행위 감사 로그를 조회하고 기록합니다.
 *
 * <p>권한 변경, 회원 변경, 배치 실행, 비밀번호 초기화처럼 운영 리스크가 있는 작업은 감사 사유와
 * 변경 전/후 데이터를 함께 남겨 사후 추적이 가능하게 합니다.</p>
 */
@Service
public class AdmAuditLogService {
    private static final Logger log = LoggerFactory.getLogger(AdmAuditLogService.class);

    private final JdbcTemplate admJdbcTemplate;

    public AdmAuditLogService(@Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate) {
        this.admJdbcTemplate = admJdbcTemplate;
    }

    public List<Map<String, Object>> findAuditLogs(
            String operatorId,
            String actionType,
            String targetType,
            String targetId,
            int limit) {

        StringBuilder sql = new StringBuilder("""
                SELECT AUDIT_ID, TRANSACTION_ID, TRACE_ID, OPERATOR_ID, MENU_ID, BUTTON_ID, ACTION_TYPE,
                       TARGET_TYPE, TARGET_ID, REASON, BEFORE_DATA, AFTER_DATA, DIFF_DATA,
                       CLIENT_IP, RETENTION_UNTIL, IMMUTABLE_YN, CREATED_AT
                FROM adm_audit_log
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        if (TextUtils.hasText(operatorId)) {
            sql.append(" AND OPERATOR_ID = ?");
            args.add(operatorId.trim());
        }
        if (TextUtils.hasText(actionType)) {
            sql.append(" AND ACTION_TYPE = ?");
            args.add(actionType.trim());
        }
        if (TextUtils.hasText(targetType)) {
            sql.append(" AND TARGET_TYPE = ?");
            args.add(targetType.trim());
        }
        if (TextUtils.hasText(targetId)) {
            sql.append(" AND TARGET_ID = ?");
            args.add(targetId.trim());
        }
        sql.append(" ORDER BY AUDIT_ID DESC LIMIT ?");
        args.add(Math.max(1, Math.min(limit, 500)));

        try {
            return admJdbcTemplate.queryForList(sql.toString(), args.toArray());
        } catch (DataAccessException ex) {
            log.warn("ADM 감사 로그 조회를 건너뜁니다. message={}", ex.getMessage());
            return List.of();
        }
    }

    public void record(
            String transactionId,
            String operatorId,
            String actionType,
            String targetType,
            String targetId,
            String reason,
            String clientIp) {
        record(transactionId, operatorId, actionType, targetType, targetId, reason, null, null, null, clientIp);
    }

    public void record(
            String transactionId,
            String operatorId,
            String actionType,
            String targetType,
            String targetId,
            String reason,
            String beforeData,
            String afterData,
            String diffData,
            String clientIp) {

        String requiredReason = requireReason(reason);
        try {
            admJdbcTemplate.update("""
                    INSERT INTO adm_audit_log (
                        TRANSACTION_ID, OPERATOR_ID, ACTION_TYPE, TARGET_TYPE, TARGET_ID,
                        REASON, BEFORE_DATA, AFTER_DATA, DIFF_DATA, CLIENT_IP,
                        RETENTION_UNTIL, IMMUTABLE_YN, CREATED_BY, UPDATED_BY
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, DATE_ADD(CURDATE(), INTERVAL 5 YEAR), 'Y', ?, ?)
                    """,
                    value(transactionId, TransactionContext.getOrCreateTransactionId()),
                    value(operatorId, "UNKNOWN"),
                    value(actionType, "UNKNOWN"),
                    targetType,
                    targetId,
                    requiredReason,
                    beforeData,
                    afterData,
                    diffData,
                    clientIp,
                    value(operatorId, "SYSTEM"),
                    value(operatorId, "SYSTEM"));
        } catch (Exception ex) {
            log.warn("ADM 감사 로그 기록을 건너뜁니다. actionType={}, targetType={}, targetId={}, message={}",
                    actionType, targetType, targetId, ex.getMessage());
        }
    }

    public String requireReason(String reason) {
        if (!TextUtils.hasText(reason)) {
            throw new CpfValidationException("감사 사유는 필수입니다.");
        }
        return reason.trim();
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
