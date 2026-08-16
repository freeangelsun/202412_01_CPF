package com.cpf.integration.api.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.result.CpfResult;

/** 동일 JVM에 존재하는 Domain Operation을 typed key로 조회·실행하는 Registry 계약입니다. */
public interface CpfDomainOperationRegistry {
    boolean has(String systemCode, String operationId);
    <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
            String systemCode, String operationId, I request, Class<O> responseType);
}
