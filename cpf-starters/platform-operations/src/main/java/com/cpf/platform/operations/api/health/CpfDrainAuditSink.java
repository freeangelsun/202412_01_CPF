package com.cpf.platform.operations.api.health;
@FunctionalInterface
/** CpfDrainAuditSink 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfDrainAuditSink { void record(CpfDrainAuditEvent event); }
