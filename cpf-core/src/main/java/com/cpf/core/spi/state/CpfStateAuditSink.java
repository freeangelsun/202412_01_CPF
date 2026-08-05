package com.cpf.core.spi.state;

import com.cpf.core.api.state.CpfStateAuditEvent;

/** Optional audit sink. Implementations must not persist raw secrets or state keys. */
@FunctionalInterface
public interface CpfStateAuditSink {
    void record(CpfStateAuditEvent event);
}
