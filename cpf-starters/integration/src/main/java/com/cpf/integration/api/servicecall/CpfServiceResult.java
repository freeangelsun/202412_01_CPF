package com.cpf.integration.api.servicecall;

import com.cpf.core.api.result.CpfResultStatus;

/**
 * 표준 서비스 호출 결과의 공개 View입니다.
 *
 * <p>문자열 status는 호환성 필드이며 신규 업무 코드는 {@link #resultStatus()}와 상태 helper를 사용합니다.</p>
 */
public record CpfServiceResult<T>(
        String status,
        CpfServiceTarget target,
        T responseBody,
        Integer httpStatus,
        Long durationMillis,
        Integer attemptCount,
        String failureCode,
        String failureMessage,
        String recoveryId,
        String recoveryAction) {

    /** 기존 8개 필드 생성자와의 Source 호환성을 유지합니다. */
    public CpfServiceResult(
            String status, CpfServiceTarget target, T responseBody, Integer httpStatus, Long durationMillis,
            Integer attemptCount, String failureCode, String failureMessage) {
        this(status, target, responseBody, httpStatus, durationMillis, attemptCount, failureCode, failureMessage, null, null);
    }

    /** 문자열 비교 없이 표준 4상태 결과를 반환합니다. */
    public CpfResultStatus resultStatus() { return CpfServiceCallOutcome.classifyStatus(status); }

    /** 호출 성공이 확정됐는지 반환합니다. */
    public boolean success() { return resultStatus() == CpfResultStatus.SUCCESS; }

    /** 업무 실패가 확정됐는지 반환합니다. */
    public boolean businessFailure() { return resultStatus() == CpfResultStatus.BUSINESS_FAILURE; }

    /** 기술 실패가 확정됐는지 반환합니다. */
    public boolean technicalFailure() { return resultStatus() == CpfResultStatus.TECHNICAL_FAILURE; }

    /** 결과불명으로 대사/확인이 필요한지 반환합니다. */
    public boolean unknown() { return resultStatus() == CpfResultStatus.UNKNOWN; }

    /** UNKNOWN 결과가 운영 대사 식별자와 연결됐는지 반환합니다. */
    public boolean recoveryRequired() { return unknown() && recoveryId != null && !recoveryId.isBlank(); }
}
