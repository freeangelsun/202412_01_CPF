package com.cpf.core.api.logging;

/** Raised when an operational command targets a stale dynamic-log-level snapshot. */
public final class DynamicLogLevelVersionConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final long expectedVersion;
    private final long actualVersion;

    public DynamicLogLevelVersionConflictException(long expectedVersion, long actualVersion) {
        super("dynamic log-level version conflict expected=" + expectedVersion + " actual=" + actualVersion);
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }

    public long expectedVersion() {
        return expectedVersion;
    }

    public long actualVersion() {
        return actualVersion;
    }
}
