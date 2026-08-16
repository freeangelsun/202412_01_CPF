package com.cpf.core.api.result;

import java.util.Objects;
import java.util.function.Function;

/**
 * Domain/External/Async 등 분산 Boundary에서 사용하는 topology-independent 표준 Result입니다.
 * 일반 Java 메서드 전체를 이 타입으로 감싸지 않고 결과불명과 복구가 필요한 Boundary에 적용합니다.
 */
public record CpfResult<T>(
        CpfResultStatus status,
        T data,
        String errorCode,
        String errorMessage,
        CpfRecoveryInfo recoveryInfo) {

    public CpfResult {
        Objects.requireNonNull(status, "status는 필수입니다.");
        if (status == CpfResultStatus.SUCCESS && data == null) {
            throw new IllegalArgumentException("SUCCESS data는 필수입니다.");
        }
        if (status != CpfResultStatus.UNKNOWN && recoveryInfo != null) {
            throw new IllegalArgumentException("recoveryInfo는 UNKNOWN 결과에서만 사용할 수 있습니다.");
        }
    }

    /** 성공이 확정된 Result를 생성합니다. */
    public static <T> CpfResult<T> success(T data) {
        return new CpfResult<>(CpfResultStatus.SUCCESS, Objects.requireNonNull(data, "data"), null, null, null);
    }

    /** 재시도 대상이 아닌 업무 실패 Result를 생성합니다. */
    public static <T> CpfResult<T> businessFailure(String code, String message) {
        return new CpfResult<>(CpfResultStatus.BUSINESS_FAILURE, null, code, message, null);
    }

    /** 기술 실패 Result를 생성합니다. */
    public static <T> CpfResult<T> technicalFailure(String code, String message) {
        return new CpfResult<>(CpfResultStatus.TECHNICAL_FAILURE, null, code, message, null);
    }

    /** 결과불명과 필수 복구 정보를 함께 생성합니다. */
    public static <T> CpfResult<T> unknown(String code, String message, CpfRecoveryInfo recoveryInfo) {
        return new CpfResult<>(CpfResultStatus.UNKNOWN, null, code, message, Objects.requireNonNull(recoveryInfo, "recoveryInfo"));
    }

    public boolean isSuccess() { return status == CpfResultStatus.SUCCESS; }
    public boolean isBusinessFailure() { return status == CpfResultStatus.BUSINESS_FAILURE; }
    public boolean isTechnicalFailure() { return status == CpfResultStatus.TECHNICAL_FAILURE; }
    public boolean isUnknown() { return status == CpfResultStatus.UNKNOWN; }

    /**
     * 성공/업무실패/기술실패/결과불명을 각각 다른 후처리로 분기합니다.
     *
     * <p>JTA를 사용하지 않는 Domain/External Call에서 "성공이면 A, 실패면 B, UNKNOWN이면 대사"처럼
     * 결과 경계를 명시적으로 처리하는 Golden Path입니다. UNKNOWN을 일반 실패로 합치지 않으므로
     * 재조회·보상·Reconcile 누락을 방지합니다.</p>
     */
    public <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super CpfResult<T>, ? extends R> onBusinessFailure,
            Function<? super CpfResult<T>, ? extends R> onTechnicalFailure,
            Function<? super CpfResult<T>, ? extends R> onUnknown) {
        Objects.requireNonNull(onSuccess, "onSuccess");
        Objects.requireNonNull(onBusinessFailure, "onBusinessFailure");
        Objects.requireNonNull(onTechnicalFailure, "onTechnicalFailure");
        Objects.requireNonNull(onUnknown, "onUnknown");
        return switch (status) {
            case SUCCESS -> onSuccess.apply(data);
            case BUSINESS_FAILURE -> onBusinessFailure.apply(this);
            case TECHNICAL_FAILURE -> onTechnicalFailure.apply(this);
            case UNKNOWN -> onUnknown.apply(this);
        };
    }

    /**
     * 업무/기술 실패를 같은 후처리로 묶고 UNKNOWN만 별도 복구 경로로 처리합니다.
     * 단순 연동 업무에서 성공/실패/결과불명의 세 갈래만 필요할 때 사용합니다.
     */
    public <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super CpfResult<T>, ? extends R> onFailure,
            Function<? super CpfResult<T>, ? extends R> onUnknown) {
        Objects.requireNonNull(onFailure, "onFailure");
        return fold(onSuccess, onFailure, onFailure, onUnknown);
    }

    /** 성공이 확정된 경우에만 데이터를 반환합니다. UNKNOWN을 성공처럼 unwrap하지 않습니다. */
    public T requireData() {
        if (!isSuccess()) throw new IllegalStateException("SUCCESS 결과가 아닙니다: " + status);
        return data;
    }
}
