package com.cpf.core.api.logging;

/** Read-only versioned state contract for operations, monitoring and audit consumers. */
public interface CpfDynamicLogLevelRuntimeStatus {
    DynamicLogLevelRuntimeSnapshot snapshot();
}
