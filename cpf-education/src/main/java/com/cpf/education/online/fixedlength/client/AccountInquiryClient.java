package com.cpf.education.online.fixedlength.client;
import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.http.CpfRestClient;
import com.cpf.integration.fixedlength.api.*;
import java.util.Map;
/** Public Fixed-Length codec + typed RestClient만 사용하는 기관 호출 Client입니다. */
public final class AccountInquiryClient {
 private final CpfFixedLengthOperations fixed; private final CpfRestClient rest;
 public AccountInquiryClient(CpfFixedLengthOperations fixed,CpfRestClient rest){this.fixed=fixed;this.rest=rest;}
 public CpfResult<String> invoke(Map<String,Object> fields){
  CpfFixedLengthWriteResult write=fixed.write(fields,"EDU-CREDIT-REQ","1");
  return rest.exchangeResult("legacy-credit","POST",b->b.path("/telegram").build(),write.message(),Map.of(),String.class);
 }
 /** parse 동작은 고정길이 전문 Public API와 기관 Outcome Adapter를 분리하는 Fixed-Length Golden Path에서 필요한 공개 동작을 수행합니다. */
 public CpfFixedLengthParseResult parse(String telegram){return fixed.parse(telegram,"EDU-CREDIT-RES","1");}
}
