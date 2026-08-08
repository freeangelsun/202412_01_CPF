package com.cpf.core.api.security.audit;
/** 변조 방지 감사 기록을 추가하고 Hash Chain/서명을 검증하는 업무 API입니다. */
public interface CpfTamperAuditOperations { CpfTamperAuditRecord append(String transactionId,String actor,String action,byte[] canonicalMaskedPayload); Verification verify(long fromSequence,int limit); record Verification(boolean valid,long checked,String failure){} }
