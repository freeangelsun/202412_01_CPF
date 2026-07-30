package com.cpf.core.api.servicecall;

import java.util.function.Function;

/** Gateway/Generated Domain 등이 Core internal 구현을 직접 import하지 않고 사용하는 서비스 호출 Public API입니다. */
public interface CpfServiceCallExecutor {
    <T> CpfServiceCallOutcome<T> invoke(CpfServiceCallCommand command, Function<CpfServiceCallTarget,T> remoteCall);

    /** Retry/Failover 개별 시도를 누락 없이 관찰하는 필수 계약입니다. 구현체는 이를 무시하거나 예외로 대체할 수 없습니다. */
    <T> CpfServiceCallOutcome<T> invoke(
            CpfServiceCallCommand command,
            Function<CpfServiceCallTarget,T> remoteCall,
            CpfServiceCallAttemptObserver observer);
}
