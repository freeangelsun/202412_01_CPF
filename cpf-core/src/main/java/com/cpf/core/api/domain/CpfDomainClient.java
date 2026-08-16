package com.cpf.core.api.domain;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.result.CpfResult;

/** 배포 위치와 무관하게 동일 Business Source가 사용하는 Typed Domain Client 계약입니다. */
@FunctionalInterface
public interface CpfDomainClient<I extends CpfRequest, O extends CpfResponse> {
    CpfResult<O> execute(I request);
}
