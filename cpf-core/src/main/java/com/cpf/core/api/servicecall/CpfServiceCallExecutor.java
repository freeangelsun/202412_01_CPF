package com.cpf.core.api.servicecall;

import java.util.function.Function;

/** Gateway/Generated Domain 등이 Core internal 구현을 직접 import하지 않고 사용하는 서비스 호출 Public API입니다. */
public interface CpfServiceCallExecutor {
    <T> CpfServiceCallOutcome<T> invoke(CpfServiceCallCommand command, Function<CpfServiceCallTarget,T> remoteCall);
}
