package com.cpf.education.online.externalrest.client;
import com.cpf.core.api.result.CpfResult;
import com.cpf.education.online.externalrest.dto.InstitutionInquiryResponse;
import com.cpf.integration.api.http.CpfRestClient;
import java.util.Map;
/** 외부기관 HTTP transport contract입니다. CPF 내부 보호 Header는 직접 전달하지 않습니다. */
public final class InstitutionInquiryClient {
    private final CpfRestClient rest;
    public InstitutionInquiryClient(CpfRestClient rest) { this.rest = rest; }
    public CpfResult<InstitutionInquiryResponse> inquire(String accountId) {
        return rest.exchangeResult("credit-agency", "GET",
                b -> b.path("/v1/accounts/{id}").build(accountId), null,
                Map.of("X-Institution-Contract", "credit-inquiry-v1"), InstitutionInquiryResponse.class);
    }
}
