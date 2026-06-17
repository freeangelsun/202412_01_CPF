package cpf.adm.opr.service;

import cpf.adm.opr.dto.AdmIpAllowlistRequest;
import cpf.adm.opr.dto.AdmMfaOtpRequest;
import cpf.cmn.utils.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ADM 보안 운영 메타를 조회하고 변경합니다.
 */
@Service
public class AdmSecurityOperationService {
    private static final Logger log = LoggerFactory.getLogger(AdmSecurityOperationService.class);

    private final JdbcTemplate admJdbcTemplate;

    public AdmSecurityOperationService(@Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate) {
        this.admJdbcTemplate = admJdbcTemplate;
    }

    public List<Map<String, Object>> findIpAllowlist() {
        try {
            return admJdbcTemplate.queryForList("""
                    SELECT ALLOW_ID, IP_PATTERN, DESCRIPTION, USE_YN, CREATED_AT, UPDATED_AT
                    FROM adm_ip_allowlist
                    ORDER BY ALLOW_ID DESC
                    """);
        } catch (DataAccessException ex) {
            log.debug("ADM IP allowlist query skipped. reason={}", ex.getMessage());
            return List.of();
        }
    }

    public Map<String, Object> upsertIpAllowlist(AdmIpAllowlistRequest request) {
        String requestUser = TextUtils.defaultIfBlank(request.requestUser(), "ADM");
        admJdbcTemplate.update("""
                INSERT INTO adm_ip_allowlist (IP_PATTERN, DESCRIPTION, USE_YN, CREATED_BY, UPDATED_BY)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    DESCRIPTION = VALUES(DESCRIPTION),
                    USE_YN = VALUES(USE_YN),
                    UPDATED_BY = VALUES(UPDATED_BY),
                    UPDATED_AT = CURRENT_TIMESTAMP
                """,
                TextUtils.requireText(request.ipPattern(), "ipPattern"),
                request.description(),
                "N".equalsIgnoreCase(request.useYn()) ? "N" : "Y",
                requestUser,
                requestUser);
        return admJdbcTemplate.queryForMap("""
                SELECT ALLOW_ID, IP_PATTERN, DESCRIPTION, USE_YN, CREATED_AT, UPDATED_AT
                FROM adm_ip_allowlist
                WHERE IP_PATTERN = ?
                """, request.ipPattern());
    }

    public List<Map<String, Object>> findMfaStates() {
        try {
            return admJdbcTemplate.queryForList("""
                    SELECT OPERATOR_ID, SECRET_REF, ENABLED_YN, VERIFIED_AT, CREATED_AT, UPDATED_AT
                    FROM adm_mfa_otp_secret
                    ORDER BY OPERATOR_ID
                    """);
        } catch (DataAccessException ex) {
            log.debug("ADM MFA state query skipped. reason={}", ex.getMessage());
            return List.of();
        }
    }

    public Map<String, Object> registerMfa(String operatorId, AdmMfaOtpRequest request) {
        String requestUser = TextUtils.defaultIfBlank(request.requestUser(), "ADM");
        admJdbcTemplate.update("""
                INSERT INTO adm_mfa_otp_secret (OPERATOR_ID, SECRET_REF, ENABLED_YN, CREATED_BY, UPDATED_BY)
                VALUES (?, ?, 'N', ?, ?)
                ON DUPLICATE KEY UPDATE
                    SECRET_REF = VALUES(SECRET_REF),
                    ENABLED_YN = 'N',
                    VERIFIED_AT = NULL,
                    UPDATED_BY = VALUES(UPDATED_BY),
                    UPDATED_AT = CURRENT_TIMESTAMP
                """, operatorId, TextUtils.requireText(request.secretRef(), "secretRef"), requestUser, requestUser);
        return findMfaState(operatorId);
    }

    public Map<String, Object> verifyMfa(String operatorId, AdmMfaOtpRequest request) {
        TextUtils.requireText(request.otpCode(), "otpCode");
        admJdbcTemplate.update("""
                UPDATE adm_mfa_otp_secret
                SET ENABLED_YN = 'Y',
                    VERIFIED_AT = CURRENT_TIMESTAMP,
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE OPERATOR_ID = ?
                """, TextUtils.defaultIfBlank(request.requestUser(), "ADM"), operatorId);
        return findMfaState(operatorId);
    }

    public Map<String, Object> disableMfa(String operatorId, String requestUser) {
        admJdbcTemplate.update("""
                UPDATE adm_mfa_otp_secret
                SET ENABLED_YN = 'N',
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE OPERATOR_ID = ?
                """, TextUtils.defaultIfBlank(requestUser, "ADM"), operatorId);
        return findMfaState(operatorId);
    }

    private Map<String, Object> findMfaState(String operatorId) {
        return admJdbcTemplate.queryForMap("""
                SELECT OPERATOR_ID, SECRET_REF, ENABLED_YN, VERIFIED_AT, CREATED_AT, UPDATED_AT
                FROM adm_mfa_otp_secret
                WHERE OPERATOR_ID = ?
                """, operatorId);
    }
}
