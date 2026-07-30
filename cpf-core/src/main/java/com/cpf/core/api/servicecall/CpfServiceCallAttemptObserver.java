package com.cpf.core.api.servicecall;

/** 시도별 원장을 유실 없이 기록하기 위한 동기 Observer입니다. 기록 실패는 호출 실패로 전파됩니다. */
@FunctionalInterface
public interface CpfServiceCallAttemptObserver {
    void onAttempt(CpfServiceCallAttempt attempt);

    static CpfServiceCallAttemptObserver noOp() {
        return ignored -> { };
    }
}
