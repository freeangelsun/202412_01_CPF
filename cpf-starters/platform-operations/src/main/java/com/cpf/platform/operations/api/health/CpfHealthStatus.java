package com.cpf.platform.operations.api.health;

/** 런타임 및 외부 의존성의 표준 상태입니다. */
public enum CpfHealthStatus {
    UP(0), DEGRADED(1), UNKNOWN(2), OUT_OF_SERVICE(3), DOWN(4);
    private final int severity;
    CpfHealthStatus(int severity) { this.severity = severity; }
    public static CpfHealthStatus worst(CpfHealthStatus left, CpfHealthStatus right) {
        if (left == null) return right == null ? UNKNOWN : right;
        if (right == null) return left;
        return left.severity >= right.severity ? left : right;
    }
}
