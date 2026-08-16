package com.cpf.integration.api.servicecall;

import com.cpf.core.api.result.CpfResultStatus;
import java.util.Locale;

/**
 * 표준 서비스 호출의 공개 결과입니다.
 *
 * <p>기존 문자열 {@code status} 계약은 호환성을 위해 유지하면서, 업무 코드는 문자열 비교 대신
 * {@link #resultStatus()}와 상태 helper를 사용해 성공/업무실패/기술실패/결과불명을 명확히 처리합니다.</p>
 */
public record CpfServiceCallOutcome<T>(
        String status,
        CpfServiceCallTarget target,
        T responseBody,
        Integer httpStatus,
        Long durationMillis,
        Integer attemptCount,
        String failureCode,
        String failureMessage,
        String recoveryId,
        String recoveryAction) {

    /** 기존 8개 필드 생성자와의 Source 호환성을 유지합니다. */
    public CpfServiceCallOutcome(
            String status, CpfServiceCallTarget target, T responseBody, Integer httpStatus, Long durationMillis,
            Integer attemptCount, String failureCode, String failureMessage) {
        this(status, target, responseBody, httpStatus, durationMillis, attemptCount, failureCode, failureMessage, null, null);
    }

    /** 문자열 상태를 표준 Boundary 결과 분류로 안전하게 변환합니다. */
    public static CpfResultStatus classifyStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "SUCCESS", "SUCCEEDED" -> CpfResultStatus.SUCCESS;
            case "BUSINESS_FAILURE", "BUSINESS_FAILED", "REJECTED" -> CpfResultStatus.BUSINESS_FAILURE;
            case "UNKNOWN", "UNKNOWN_RESULT" -> CpfResultStatus.UNKNOWN;
            case "TECHNICAL_FAILURE", "TECHNICAL_FAILED", "FAILED", "ERROR" -> CpfResultStatus.TECHNICAL_FAILURE;
            default -> CpfResultStatus.UNKNOWN;
        };
    }

    /** 현재 문자열 상태를 표준 Boundary 결과 분류로 반환합니다. */
    public CpfResultStatus resultStatus() {
        return classifyStatus(status);
    }

    /** 호출이 성공으로 확정됐는지 반환합니다. */
    public boolean success() { return resultStatus() == CpfResultStatus.SUCCESS; }

    /** 업무 규칙에 의해 실패가 확정됐는지 반환합니다. */
    public boolean businessFailure() { return resultStatus() == CpfResultStatus.BUSINESS_FAILURE; }

    /** 기술적 실패로 확정됐는지 반환합니다. */
    public boolean technicalFailure() { return resultStatus() == CpfResultStatus.TECHNICAL_FAILURE; }

    /** Side effect 결과를 확정할 수 없어 Reconcile이 필요한지 반환합니다. */
    public boolean unknown() { return resultStatus() == CpfResultStatus.UNKNOWN; }

    /** UNKNOWN 결과가 운영 대사/수동 확인과 연결됐는지 반환합니다. */
    public boolean recoveryRequired() { return unknown() && recoveryId != null && !recoveryId.isBlank(); }
}
