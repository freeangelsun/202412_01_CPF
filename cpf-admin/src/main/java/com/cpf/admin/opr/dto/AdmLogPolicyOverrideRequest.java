package cpf.adm.opr.dto;

import java.time.LocalDateTime;

/**
 * ADM 로그 정책 임시 override 요청입니다.
 */
public record AdmLogPolicyOverrideRequest(
        Long policyId,
        String targetType,
        String targetId,
        String logLevel,
        String dbLogEnabledYn,
        String fileLogEnabledYn,
        String requestBodyLogYn,
        String responseBodyLogYn,
        String errorStackLogYn,
        String maskingPolicyKey,
        LocalDateTime effectiveStartAt,
        LocalDateTime effectiveEndAt,
        String approvedBy,
        String requestUser,
        String reason) {
}
