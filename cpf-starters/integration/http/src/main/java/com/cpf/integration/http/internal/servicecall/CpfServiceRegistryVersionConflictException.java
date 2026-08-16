package com.cpf.integration.http.internal.servicecall;

/** Service Registry optimistic-lock 충돌을 Owner 내부에서 표현합니다. */
final class CpfServiceRegistryVersionConflictException extends IllegalStateException {
    private final long expectedVersion;
    private final long currentVersion;

    CpfServiceRegistryVersionConflictException(long expectedVersion, long currentVersion) {
        super("Service Registry version conflict: expected=" + expectedVersion + ", current=" + currentVersion);
        this.expectedVersion = expectedVersion;
        this.currentVersion = currentVersion;
    }

    long expectedVersion() { return expectedVersion; }
    long currentVersion() { return currentVersion; }
}
