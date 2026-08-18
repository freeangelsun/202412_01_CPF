package com.cpf.education.online.domaincall.client;
import com.cpf.core.api.result.CpfResult;
import com.cpf.education.online.domaincall.dto.CreditCheckRequest;
import com.cpf.education.online.domaincall.dto.CreditCheckResponse;
import com.cpf.integration.api.domaincall.CpfDomainClientRouter;
/** MBR 업무가 EXS_CREDIT_CHECK Target Contract를 호출하는 typed Domain Client입니다. */
public final class CreditCheckDomainClient {
    private final CpfDomainClientRouter domains;
    public CreditCheckDomainClient(CpfDomainClientRouter domains) { this.domains = domains; }
    public CpfResult<CreditCheckResponse> inquire(CreditCheckRequest request) {
        return domains.invoke("EXS", "EXS_CREDIT_CHECK", request, CreditCheckResponse.class);
    }
}
