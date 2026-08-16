package com.cpf.platform.operations.runtimecontrol;

/** Runtime Control Plane의 분산 명령 처리량 제한을 초과했음을 나타냅니다. */
public class CpfRuntimeRateLimitException extends RuntimeException {
    private final int limit;

    public CpfRuntimeRateLimitException(int limit) {
        super("Runtime Control Plane 명령 제한을 초과했습니다. limitPerMinute=" + limit);
        this.limit = limit;
    }

    public int limit() {
        return limit;
    }
}
