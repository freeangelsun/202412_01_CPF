package cpf.adm.opr.service;

import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.FpsValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                SELECT AUDIT_ID, TRANSACTION_ID, TRACE_ID, OPERATOR_ID, MENU_ID, ACTION_TYPE,
                       TARGET_TYPE, TARGET_ID, REASON, CLIENT_IP, CREATED_AT
                FROM operator_audit_log
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
            log.warn("ADM audit log query skipped. message={}", ex.getMessage());
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

        String requiredReason = requireReason(reason);
        try {
            admJdbcTemplate.update("""
                    INSERT INTO operator_audit_log (
                        TRANSACTION_ID, OPERATOR_ID, ACTION_TYPE, TARGET_TYPE, TARGET_ID,
                        REASON, CLIENT_IP, CREATED_BY, UPDATED_BY
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    value(transactionId, "NO_TRANSACTION"),
                    value(operatorId, "UNKNOWN"),
                    value(actionType, "UNKNOWN"),
                    targetType,
                    targetId,
                    requiredReason,
                    clientIp,
                    value(operatorId, "SYSTEM"),
                    value(operatorId, "SYSTEM"));
        } catch (Exception ex) {
            log.warn("ADM audit log write skipped. actionType={}, targetType={}, targetId={}, message={}",
                    actionType, targetType, targetId, ex.getMessage());
        }
    }

    public String requireReason(String reason) {
        if (!TextUtils.hasText(reason)) {
            throw new FpsValidationException("Audit reason is required.");
        }
        return reason.trim();
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
