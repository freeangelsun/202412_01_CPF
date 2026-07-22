package cpf.adm.opr.dto;

import java.math.BigDecimal;

/**
 * ADM 로그 정책 저장 요청입니다.
 */
public record AdmLogPolicyRequest(
        String policyKey,
        String policyName,
        String targetType,
        String targetId,
        String logLevel,
        String dbLogEnabledYn,
        String fileLogEnabledYn,
        String requestBodyLogYn,
        String responseBodyLogYn,
        String errorStackLogYn,
        String maskingPolicyKey,
        Integer retentionDays,
        BigDecimal samplingRate,
        Integer priority,
        String activeYn,
        String description,
        String requestUser,
        String reason) {
}
