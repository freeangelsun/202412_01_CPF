package com.cpf.core.api.async;
/** Handler가 cooperative cancellation만 확인하는 최소 실행 Control입니다. Heartbeat는 Framework가 관리합니다. */
public interface CpfAsyncExecution {
    String executionId();
    boolean cancellationRequested();
}
