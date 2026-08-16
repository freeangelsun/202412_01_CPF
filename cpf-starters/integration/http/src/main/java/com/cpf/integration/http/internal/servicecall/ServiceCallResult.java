package com.cpf.integration.http.internal.servicecall;

import com.cpf.core.api.result.CpfResultStatus;

/**
 * 서비스 호출 엔진 실행 결과입니다.
 *
 * <p>응답 본문, 선택 instance, HTTP status, 실패/복구 정보를 분리해 ADM 관제와 call history가
 * 같은 기준으로 조회할 수 있게 합니다.</p>
 */
public record ServiceCallResult<T>(
        String status, ServiceCallResolvedTarget target, T responseBody, Integer httpStatus, Long durationMillis,
        Integer attemptCount, String failureCode, String failureMessage, String recoveryId, String recoveryAction) {

    /** 내부 transport가 문자열 비교 없이 상태를 판정하도록 표준 분류를 제공합니다. */
    public CpfResultStatus resultStatus() {
        return com.cpf.integration.api.servicecall.CpfServiceCallOutcome.classifyStatus(status);
    }

    public boolean successValue() {
        return resultStatus() == CpfResultStatus.SUCCESS;
    }

    public boolean businessFailureValue() {
        return resultStatus() == CpfResultStatus.BUSINESS_FAILURE;
    }

    public boolean technicalFailureValue() {
        return resultStatus() == CpfResultStatus.TECHNICAL_FAILURE;
    }

    public boolean unknownValue() {
        return resultStatus() == CpfResultStatus.UNKNOWN;
    }

    /** 성공 결과를 생성합니다. */
    public static <T> ServiceCallResult<T> success(ServiceCallResolvedTarget target, T body, Integer httpStatus, Long duration, Integer attempts) {
        return new ServiceCallResult<>("SUCCESS", target, body, httpStatus, duration, attempts, null, null, null, null);
    }

    /** 단일 시도 성공 결과를 생성합니다. */
    public static <T> ServiceCallResult<T> success(ServiceCallResolvedTarget target, T body, Integer httpStatus, Long duration) {
        return success(target, body, httpStatus, duration, 1);
    }

    /** 업무 규칙에 의해 확정된 실패 결과를 생성합니다. */
    public static <T> ServiceCallResult<T> businessFailure(ServiceCallResolvedTarget target, Integer httpStatus, Long duration, Integer attempts, String code, String message) {
        return new ServiceCallResult<>("BUSINESS_FAILURE", target, null, httpStatus, duration, attempts, code, message, null, null);
    }

    /** 기술 실패 결과를 생성합니다. */
    public static <T> ServiceCallResult<T> failure(ServiceCallResolvedTarget target, Integer httpStatus, Long duration, Integer attempts, String code, String message) {
        return new ServiceCallResult<>("TECHNICAL_FAILURE", target, null, httpStatus, duration, attempts, code, message, null, null);
    }

    /** 단일 시도 기술 실패 결과를 생성합니다. */
    public static <T> ServiceCallResult<T> failure(ServiceCallResolvedTarget target, Long duration, String code, String message) {
        return failure(target, null, duration, 1, code, message);
    }

    /** 결과불명과 운영 대사 연결 정보를 함께 생성합니다. */
    public static <T> ServiceCallResult<T> unknown(ServiceCallResolvedTarget target, Long duration, Integer attempts, String code, String message, String recoveryId, String recoveryAction) {
        return new ServiceCallResult<>("UNKNOWN", target, null, null, duration, attempts, code, message, recoveryId, recoveryAction);
    }
}
