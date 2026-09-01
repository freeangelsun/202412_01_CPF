package com.cpf.education.online.fixedlength.service;
import com.cpf.core.api.result.CpfResult;
import com.cpf.education.online.fixedlength.adapter.AccountInquiryOutcomeAdapter;
import com.cpf.education.online.fixedlength.client.AccountInquiryClient;
import com.cpf.education.online.fixedlength.dto.AccountInquiryRequest;
import com.cpf.foundation.annotation.CpfService;
import com.cpf.integration.fixedlength.api.CpfFixedLengthParseResult;
@CpfService
/** AccountInquiryService는 고정길이 전문 Public API와 기관 Outcome Adapter를 분리하는 Fixed-Length Golden Path입니다. */
public class AccountInquiryService {
 private final AccountInquiryClient client; private final AccountInquiryOutcomeAdapter adapter;
 /** AccountInquiryService 동작은 고정길이 전문 Public API와 기관 Outcome Adapter를 분리하는 Fixed-Length Golden Path에서 필요한 공개 동작을 수행합니다. */
 public AccountInquiryService(AccountInquiryClient client,AccountInquiryOutcomeAdapter adapter){this.client=client;this.adapter=adapter;}
 /** inquire 동작은 고정길이 전문 Public API와 기관 Outcome Adapter를 분리하는 Fixed-Length Golden Path에서 필요한 공개 동작을 수행합니다. */
 public CpfResult<CpfFixedLengthParseResult> inquire(AccountInquiryRequest request){return adapter.adapt(client.invoke(request.fields()),client::parse);}
}
