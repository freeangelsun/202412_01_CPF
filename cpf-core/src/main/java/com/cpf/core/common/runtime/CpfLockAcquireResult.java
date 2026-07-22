package com.cpf.core.common.runtime;

/**
 * 분산 lock 획득 결과입니다.
 */
public record CpfLockAcquireResult(
        boolean acquired,
        CpfLockHandle lockHandle,
        String detail) {
}
