package com.cpf.education.online.fixedlength.adapter;
import com.cpf.core.api.result.*;
import com.cpf.integration.fixedlength.api.CpfFixedLengthParseResult;
/** 기관 응답전문을 CPF 공통 Boundary Outcome으로 해석합니다. */
public final class AccountInquiryOutcomeAdapter {
 public CpfResult<CpfFixedLengthParseResult> adapt(CpfResult<String> transport,java.util.function.Function<String,CpfFixedLengthParseResult> parser){
  return transport.fold(raw->{
    CpfFixedLengthParseResult parsed=parser.apply(raw);
    if(!parsed.valid()) return CpfResult.technicalFailure("CPF-FIXED-PARSE","기관 응답전문 형식이 유효하지 않습니다.");
    String code=parsed.fields().getOrDefault("responseCode","");
    return "0000".equals(code)?CpfResult.success(parsed):CpfResult.businessFailure(code.isBlank()?"INSTITUTION-BUSINESS":code,"기관 업무 응답이 실패했습니다.");
  }, r->CpfResult.businessFailure(r.errorCode(),r.errorMessage()), r->CpfResult.technicalFailure(r.errorCode(),r.errorMessage()), r->CpfResult.unknown(r.errorCode(),r.errorMessage(),r.recoveryInfo()));
 }
}
