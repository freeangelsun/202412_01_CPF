package com.cpf.core.common.runtimecontrol;

/** Runtime Control Plane 분산 명령 제한 초과입니다. */
public class CpfRuntimeRateLimitException extends RuntimeException {
    private final int limit;
    public CpfRuntimeRateLimitException(int limit) {
        super("Runtime Control Plane 명령 제한을 초과했습니다. limitPerMinute=" + limit);
        this.limit = limit;
    }
    public int limit() { return limit; }
}
