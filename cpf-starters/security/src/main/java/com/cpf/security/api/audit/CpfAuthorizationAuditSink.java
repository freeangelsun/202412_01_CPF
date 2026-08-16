package com.cpf.security.api.audit;
/** CpfAuthorizationAuditSink 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
@FunctionalInterface public interface CpfAuthorizationAuditSink { void record(CpfAuthorizationAuditEvent event); }
