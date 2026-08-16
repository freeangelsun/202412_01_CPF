package com.cpf.platform.operations.spi.state;

import com.cpf.platform.operations.api.state.CpfStateAuditEvent;

/** Optional audit sink. Implementations must not persist raw secrets or state keys. */
@FunctionalInterface
/** CpfStateAuditSink 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfStateAuditSink {
    void record(CpfStateAuditEvent event);
}
