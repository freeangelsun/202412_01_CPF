package com.cpf.education.online.domaincall.service;
import com.cpf.core.api.result.CpfResult;
import com.cpf.education.online.domaincall.client.CreditCheckDomainClient;
import com.cpf.education.online.domaincall.dto.CreditCheckRequest;
import com.cpf.education.online.domaincall.dto.CreditCheckResponse;
import com.cpf.foundation.annotation.CpfService;
/** 현재 MBR Operation과 EXS Target Operation을 분리해 호출하는 업무 Service입니다. */
@CpfService
public class MemberCreditService {
    private final CreditCheckDomainClient creditChecks;
    public MemberCreditService(CreditCheckDomainClient creditChecks) { this.creditChecks = creditChecks; }
    public CpfResult<CreditCheckResponse> check(String memberId) {
        return creditChecks.inquire(new CreditCheckRequest(memberId));
    }
}
