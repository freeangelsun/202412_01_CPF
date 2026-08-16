package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmIpAllowlistRequest;
import com.cpf.admin.opr.dto.AdmMfaOtpRequest;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.foundation.util.CpfStrings;
import com.cpf.security.api.secret.CpfSecretProvider;
import com.cpf.security.api.secret.CpfSecretReference;
import com.cpf.security.api.secret.CpfSecretValue;
import com.cpf.admin.opr.security.AdmTotpVerifier;
import com.cpf.core.api.error.CpfValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import com.cpf.foundation.annotation.CpfService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ADM 보안 운영 메타를 조회하고 변경합니다.
 */
@CpfService
public class AdmSecurityOperationService extends com.cpf.admin.common.base.AdmBaseService {
    private final JdbcTemplate admJdbcTemplate;
    private final List<CpfSecretProvider> secretProviders;
    private final AdmTotpVerifier totpVerifier;

    /** Source-compatible constructor for focused unit tests; production uses the autowired constructor. */
    public AdmSecurityOperationService(JdbcTemplate admJdbcTemplate) {
        this(admJdbcTemplate, List.of(), new AdmTotpVerifier());
    }

    @Autowired
    public AdmSecurityOperationService(
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate,
            List<CpfSecretProvider> secretProviders,
            AdmTotpVerifier totpVerifier) {
        this.admJdbcTemplate = admJdbcTemplate;
        this.secretProviders = List.copyOf(secretProviders);
        this.totpVerifier = totpVerifier;
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
        CpfSecretReference reference = parseReference(secretRef);
        provider(reference.provider());
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
        String otpCode = CpfStrings.requireText(request.otpCode(), "otpCode");
        Map<String,Object> state = findMfaState(operatorId);
        verifyReferencedTotp(string(state, "SECRET_REF"), otpCode);
        int updated = admJdbcTemplate.update("""
                UPDATE adm_mfa_otp_secret
                SET ENABLED_YN = 'Y',
                    VERIFIED_AT = CURRENT_TIMESTAMP,
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE OPERATOR_ID = ? AND SECRET_REF = ?
                """, CpfStrings.defaultIfBlank(request.requestUser(), "ADM"), operatorId, string(state, "SECRET_REF"));
        if (updated != 1) throw new CpfValidationException("MFA 등록 상태가 동시에 변경되었습니다.");
        return findMfaState(operatorId);
    }

    /** Password 인증 이후, 활성화된 MFA가 있으면 TOTP를 반드시 검증합니다. */
    public void requireMfaForLogin(String operatorId, String otpCode) {
        Map<String,Object> state;
        try {
            state = findMfaState(operatorId);
        } catch (org.springframework.dao.EmptyResultDataAccessException noRegistration) {
            return;
        }
        if (!"Y".equalsIgnoreCase(string(state, "ENABLED_YN"))) return;
        verifyReferencedTotp(string(state, "SECRET_REF"), CpfStrings.requireText(otpCode, "otpCode"));
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

    private void verifyReferencedTotp(String secretRef, String otpCode) {
        CpfSecretReference reference = parseReference(secretRef);
        CpfSecretProvider secretProvider = provider(reference.provider());
        try (CpfSecretValue value = secretProvider.resolve(reference)) {
            if (!totpVerifier.verify(value, otpCode)) {
                throw new CpfValidationException("MFA 인증에 실패했습니다.");
            }
        } catch (CpfValidationException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new CpfBusinessException(
                    CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE,
                    "MFA Secret Provider를 사용할 수 없습니다.",
                    Map.of("0", reference.provider(), "1", ex.getClass().getSimpleName()));
        }
    }

    private CpfSecretProvider provider(String providerId) {
        return secretProviders.stream()
                .filter(candidate -> candidate.providerId().equalsIgnoreCase(providerId))
                .findFirst()
                .orElseThrow(() -> new CpfBusinessException(
                        CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE,
                        "MFA Secret Provider가 구성되지 않았습니다.",
                        Map.of("0", providerId)));
    }

    private static CpfSecretReference parseReference(String raw) {
        String value = CpfStrings.requireText(raw, "secretRef");
        int scheme = value.indexOf("://");
        if (scheme > 0 && scheme + 3 < value.length()) {
            return new CpfSecretReference(value.substring(0, scheme), value.substring(scheme + 3));
        }
        int colon = value.indexOf(':');
        if (colon > 0 && colon + 1 < value.length()) {
            return new CpfSecretReference(value.substring(0, colon), value.substring(colon + 1));
        }
        throw new CpfValidationException("secretRef는 provider:key 또는 provider://key 형식이어야 합니다.");
    }

    private static String string(Map<String,Object> source, String key) {
        Object value = source.get(key);
        if (value == null) value = source.get(key.toLowerCase(java.util.Locale.ROOT));
        if (value == null) throw new CpfValidationException("MFA 상태에 " + key + "가 없습니다.");
        return String.valueOf(value);
    }

    private CpfBusinessException unavailable(String component, DataAccessException ex) {
        return new CpfBusinessException(
                CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE,
                "ADM 보안 운영 저장소를 사용할 수 없습니다.",
                Map.of("0", component, "1", ex.getClass().getSimpleName()));
    }
}
