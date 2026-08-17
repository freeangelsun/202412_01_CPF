package com.cpf.integration.api.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.domain.CpfDomainBinding;
import com.cpf.core.api.result.CpfResult;

/** Registry/Router가 선택한 원격 Domain에 transport 세부정보를 숨겨 전달하는 SPI입니다. */
public interface CpfDomainRemoteTransport {
    <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
            String systemCode, String operationId, CpfDomainBinding binding, I request, Class<O> responseType);

    /** Optional custom Header를 포함한 호출. 기존 transport는 옵션을 무시해 호환성을 유지합니다. */
    default <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
            String systemCode, String operationId, CpfDomainBinding binding, I request, Class<O> responseType,
            CpfDomainCallOptions options) {
        return invoke(systemCode, operationId, binding, request, responseType);
    }
}
