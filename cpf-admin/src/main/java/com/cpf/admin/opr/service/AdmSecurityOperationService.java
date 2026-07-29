package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmIpAllowlistRequest;
import com.cpf.admin.opr.dto.AdmMfaOtpRequest;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.util.CpfStrings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ADM 보안 운영 메타를 조회하고 변경합니다.
 */
@Service
public class AdmSecurityOperationService extends com.cpf.admin.common.base.AdmBaseService {
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
            throw unavailable("adm_ip_allowlist.list", ex);
        }
    }

    public Map<String, Object> upsertIpAllowlist(AdmIpAllowlistRequest request) {
        String ipPattern = CpfStrings.requireText(request.ipPattern(), "ipPattern");
        String requestUser = CpfStrings.defaultIfBlank(request.requestUser(), "ADM");
        String useYn = "N".equalsIgnoreCase(request.useYn()) ? "N" : "Y";
        if (updateIpAllowlist(ipPattern, request.description(), useYn, requestUser) == 0) {
            try {
                admJdbcTemplate.update("""
                        INSERT INTO adm_ip_allowlist (
                            IP_PATTERN, DESCRIPTION, USE_YN, CREATED_BY, UPDATED_BY
                        ) VALUES (?, ?, ?, ?, ?)
                        """, ipPattern, request.description(), useYn, requestUser, requestUser);
            } catch (DuplicateKeyException concurrentInsert) {
                if (updateIpAllowlist(ipPattern, request.description(), useYn, requestUser) == 0) {
                    throw concurrentInsert;
                }
            }
        }
        return admJdbcTemplate.queryForMap("""
                SELECT ALLOW_ID, IP_PATTERN, DESCRIPTION, USE_YN, CREATED_AT, UPDATED_AT
                FROM adm_ip_allowlist
                WHERE IP_PATTERN = ?
                """, ipPattern);
    }

    public List<Map<String, Object>> findMfaStates() {
        try {
            return admJdbcTemplate.queryForList("""
                    SELECT OPERATOR_ID, SECRET_REF, ENABLED_YN, VERIFIED_AT, CREATED_AT, UPDATED_AT
                    FROM adm_mfa_otp_secret
                    ORDER BY OPERATOR_ID
                    """);
        } catch (DataAccessException ex) {
            throw unavailable("adm_mfa_otp_secret.list", ex);
        }
    }

    public Map<String, Object> registerMfa(String operatorId, AdmMfaOtpRequest request) {
        String secretRef = CpfStrings.requireText(request.secretRef(), "secretRef");
        String requestUser = CpfStrings.defaultIfBlank(request.requestUser(), "ADM");
        if (updateMfaRegistration(operatorId, secretRef, requestUser) == 0) {
            try {
                admJdbcTemplate.update("""
                        INSERT INTO adm_mfa_otp_secret (
                            OPERATOR_ID, SECRET_REF, ENABLED_YN, CREATED_BY, UPDATED_BY
                        ) VALUES (?, ?, 'N', ?, ?)
                        """, operatorId, secretRef, requestUser, requestUser);
            } catch (DuplicateKeyException concurrentInsert) {
                if (updateMfaRegistration(operatorId, secretRef, requestUser) == 0) {
                    throw concurrentInsert;
                }
            }
        }
        return findMfaState(operatorId);
    }

    public Map<String, Object> verifyMfa(String operatorId, AdmMfaOtpRequest request) {
        CpfStrings.requireText(request.otpCode(), "otpCode");
        admJdbcTemplate.update("""
                UPDATE adm_mfa_otp_secret
                SET ENABLED_YN = 'Y',
                    VERIFIED_AT = CURRENT_TIMESTAMP,
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE OPERATOR_ID = ?
                """, CpfStrings.defaultIfBlank(request.requestUser(), "ADM"), operatorId);
        return findMfaState(operatorId);
    }

    public Map<String, Object> disableMfa(String operatorId, String requestUser) {
        admJdbcTemplate.update("""
                UPDATE adm_mfa_otp_secret
                SET ENABLED_YN = 'N',
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE OPERATOR_ID = ?
                """, CpfStrings.defaultIfBlank(requestUser, "ADM"), operatorId);
        return findMfaState(operatorId);
    }

    private Map<String, Object> findMfaState(String operatorId) {
        return admJdbcTemplate.queryForMap("""
                SELECT OPERATOR_ID, SECRET_REF, ENABLED_YN, VERIFIED_AT, CREATED_AT, UPDATED_AT
                FROM adm_mfa_otp_secret
                WHERE OPERATOR_ID = ?
                """, operatorId);
    }

    private int updateIpAllowlist(String ipPattern, String description, String useYn, String requestUser) {
        return admJdbcTemplate.update("""
                UPDATE adm_ip_allowlist
                SET DESCRIPTION = ?,
                    USE_YN = ?,
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE IP_PATTERN = ?
                """, description, useYn, requestUser, ipPattern);
    }

    private int updateMfaRegistration(String operatorId, String secretRef, String requestUser) {
        return admJdbcTemplate.update("""
                UPDATE adm_mfa_otp_secret
                SET SECRET_REF = ?,
                    ENABLED_YN = 'N',
                    VERIFIED_AT = NULL,
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE OPERATOR_ID = ?
                """, secretRef, requestUser, operatorId);
    }

    private CpfBusinessException unavailable(String component, DataAccessException ex) {
        return new CpfBusinessException(
                CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE,
                "ADM 보안 운영 저장소를 사용할 수 없습니다.",
                Map.of("0", component, "1", ex.getClass().getSimpleName()));
    }
}
