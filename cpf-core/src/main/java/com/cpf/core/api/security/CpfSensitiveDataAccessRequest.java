package com.cpf.core.api.security;

/**
 * 원격 로그/감사 원문 조회 요청에 필요한 사유 계약입니다.
 * 사유는 생성 시 공통 감사 마스킹 정책으로 정제되며 문자열 표현에는 노출되지 않습니다.
 */
public record CpfSensitiveDataAccessRequest(String reason) {
    public CpfSensitiveDataAccessRequest {
        try {
            reason = CpfSensitiveData.sanitizeAuditReason(reason);
        } catch (RuntimeException invalidReason) {
            throw new IllegalArgumentException(
                    "sensitive-data access reason must be 10..500 safe characters", invalidReason);
        }
        if (reason.length() < 10 || reason.length() > 500
                || reason.indexOf('\r') >= 0 || reason.indexOf('\n') >= 0) {
            throw new IllegalArgumentException(
                    "sensitive-data access reason must be 10..500 safe characters");
        }
    }

    @Override
    public String toString() {
        return "CpfSensitiveDataAccessRequest[reason=[REDACTED]]";
    }
}
