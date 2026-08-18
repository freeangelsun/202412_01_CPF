package com.cpf.education.online.externalrest.service;
import com.cpf.core.api.result.CpfResult;
import com.cpf.education.online.externalrest.adapter.InstitutionOutcomeAdapter;
import com.cpf.education.online.externalrest.client.InstitutionInquiryClient;
import com.cpf.education.online.externalrest.dto.InstitutionInquiryResponse;
import com.cpf.foundation.annotation.CpfService;
@CpfService
/** InstitutionInquiryService는 기관 REST 응답을 CPF 공통 Outcome으로 변환하는 External REST Golden Path입니다. */
public class InstitutionInquiryService {
    private final InstitutionInquiryClient client; private final InstitutionOutcomeAdapter outcomes;
    /** InstitutionInquiryService 동작은 기관 REST 응답을 CPF 공통 Outcome으로 변환하는 External REST Golden Path에서 필요한 공개 동작을 수행합니다. */
    public InstitutionInquiryService(InstitutionInquiryClient client, InstitutionOutcomeAdapter outcomes){this.client=client;this.outcomes=outcomes;}
    /** inquire 동작은 기관 REST 응답을 CPF 공통 Outcome으로 변환하는 External REST Golden Path에서 필요한 공개 동작을 수행합니다. */
    public CpfResult<InstitutionInquiryResponse> inquire(String accountId){return outcomes.classify(client.inquire(accountId));}
}
