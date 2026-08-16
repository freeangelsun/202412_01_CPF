package com.cpf.integration.api.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.domain.CpfDomainBinding;
import com.cpf.core.api.result.CpfResult;

/** Registry/Router가 선택한 원격 Domain에 transport 세부정보를 숨겨 전달하는 SPI입니다. */
public interface CpfDomainRemoteTransport {
    <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
            String systemCode, String operationId, CpfDomainBinding binding, I request, Class<O> responseType);
}
