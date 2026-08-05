package com.cpf.core.api.runtimecontrol;

/** Runtime 변경의 optimistic version 비교가 실패했음을 나타냅니다. */
public class CpfRuntimeVersionConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final long expectedVersion;
    private final long actualVersion;

    public CpfRuntimeVersionConflictException(long expectedVersion, long actualVersion) {
        super("Runtime version conflict. expected=" + expectedVersion + ", actual=" + actualVersion);
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
