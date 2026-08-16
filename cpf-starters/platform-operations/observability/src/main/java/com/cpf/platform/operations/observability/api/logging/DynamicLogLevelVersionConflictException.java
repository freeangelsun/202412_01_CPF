package com.cpf.platform.operations.observability.api.logging;

/** Raised when an operational command targets a stale dynamic-log-level snapshot. */
/** DynamicLogLevelVersionConflictException 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class DynamicLogLevelVersionConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final long expectedVersion;
    private final long actualVersion;

    public DynamicLogLevelVersionConflictException(long expectedVersion, long actualVersion) {
        super("dynamic log-level version conflict expected=" + expectedVersion + " actual=" + actualVersion);
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }

    /** expectedVersion 작업을 CPF 표준 계약에 따라 수행한다. */
    public long expectedVersion() {
        return expectedVersion;
    }

    public long actualVersion() {
        return actualVersion;
    }
}
