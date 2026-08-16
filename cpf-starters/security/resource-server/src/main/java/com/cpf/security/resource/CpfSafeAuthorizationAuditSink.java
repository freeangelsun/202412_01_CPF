package com.cpf.security.resource;
import com.cpf.security.api.audit.*;import org.slf4j.Logger;import org.slf4j.LoggerFactory;
/** 보안 판단 payload를 기록하지 않는 기본 구조화 audit sink입니다. */
public final class CpfSafeAuthorizationAuditSink implements CpfAuthorizationAuditSink{private static final Logger log=LoggerFactory.getLogger(CpfSafeAuthorizationAuditSink.class);public void record(CpfAuthorizationAuditEvent e){log.info("CPF SECURITY type={} action={} tx={} exec={} allowed={} reason={}",e.type(),e.action(),e.transactionId(),e.executionId(),e.allowed(),e.reason());}}
