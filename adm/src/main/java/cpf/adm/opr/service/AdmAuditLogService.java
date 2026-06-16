package cpf.adm.opr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdmAuditLogService {
    private static final Logger log = LoggerFactory.getLogger(AdmAuditLogService.class);

    private final JdbcTemplate admJdbcTemplate;

    public AdmAuditLogService(@Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate) {
        this.admJdbcTemplate = admJdbcTemplate;
    }

    public void record(String transactionId, String operatorId, String actionType, String targetType, String targetId, String reason, String clientIp) {
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
                    reason,
                    clientIp,
                    value(operatorId, "SYSTEM"),
                    value(operatorId, "SYSTEM"));
        } catch (Exception ex) {
            log.warn("ADM audit log write skipped. actionType={}, targetType={}, targetId={}, message={}",
                    actionType, targetType, targetId, ex.getMessage());
        }
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
