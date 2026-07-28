package com.cpf.core.common.reconciliation;
/** 외부 시스템별 결과 재조회 Adapter입니다. */
public interface CpfReconciliationProbePort {
 boolean supports(String unknownType);
 ProbeResult probe(CpfUnknownResultRecord record);
 enum Outcome { CONFIRMED_SUCCESS, CONFIRMED_FAILED, PENDING }
 record ProbeResult(Outcome outcome,String reason){public ProbeResult{outcome=outcome==null?Outcome.PENDING:outcome;reason=reason==null?"":reason;}}
}
