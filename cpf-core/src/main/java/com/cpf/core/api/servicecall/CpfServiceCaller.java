package com.cpf.core.api.servicecall;

/** registry/health/retry/failover/circuit/UNKNOWN 정책을 적용하는 공개 ServiceCall 진입점입니다. */
public interface CpfServiceCaller {
    <T> CpfServiceResult<T> invoke(CpfServiceRequest request, CpfServiceTransport<T> transport);
}
