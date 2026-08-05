package com.cpf.core.api.runtimecontrol;

/** Runtime Control Plane의 분산 명령 처리량 제한을 초과했음을 나타냅니다. */
public class CpfRuntimeRateLimitException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int limit;

    public CpfRuntimeRateLimitException(int limit) {
        super("Runtime Control Plane 명령 제한을 초과했습니다. limitPerMinute=" + limit);
        this.limit = limit;
    }

    /** Remote Control Plane이 한도를 공개하지 않은 429 응답을 전달할 때 사용합니다. */
    public CpfRuntimeRateLimitException(String message) {
        super(message == null || message.isBlank()
                ? "Runtime Control Plane 명령 제한을 초과했습니다."
                : message.trim());
        this.limit = -1;
    }

    public int limit() {
        return limit;
    }
}
