package com.cpf.starter.platform.operations.observability;

import com.cpf.core.api.context.CpfContextSnapshot;
import org.slf4j.MDC;

public final class CpfMdcContextProjection {
    private static final String[] KEYS = {
        "cpf.transactionId", "cpf.executionId", "cpf.segmentId", "cpf.correlationId", "cpf.tenantId"
    };

    public void bind(CpfContextSnapshot snapshot) {
        clear();
        MDC.put(KEYS[0], snapshot.transaction().transactionId());
        MDC.put(KEYS[1], snapshot.execution().executionId());
        MDC.put(KEYS[2], snapshot.execution().segmentId());
        if (snapshot.transaction().correlationId() != null) {
            MDC.put(KEYS[3], snapshot.transaction().correlationId());
        }
        if (snapshot.tenant() != null && snapshot.tenant().tenantId() != null) {
            MDC.put(KEYS[4], snapshot.tenant().tenantId());
        }
    }

    public void clear() {
        for (String key : KEYS) {
            MDC.remove(key);
        }
    }
}
