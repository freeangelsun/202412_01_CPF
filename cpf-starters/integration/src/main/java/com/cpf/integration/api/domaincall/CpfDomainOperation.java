package com.cpf.integration.api.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.result.CpfResult;

/** 같은 JVM에서 Remote와 동일 의미로 실행되는 managed Domain Operation 계약입니다. */
public interface CpfDomainOperation<I extends CpfRequest, O extends CpfResponse> {
    String systemCode();
    String operationId();
    Class<I> requestType();
    Class<O> responseType();
    CpfResult<O> invoke(I request);
}
