package com.cpf.platform.operations.runtimecontrol;

import java.util.Locale;

/** Agent가 Control Plane에 보고할 수 있는 ACK 결과입니다. SUCCESS는 저장 시 ACKED로 정규화됩니다. */
public enum CpfRuntimeAckState {
    SUCCESS,
    ACKED,
    FAILED,
    UNKNOWN_RESULT,
    RESTART_REQUIRED;

    public static CpfRuntimeAckState require(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Runtime ACK state가 필요합니다.");
        try { return valueOf(value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("지원하지 않는 Runtime ACK state: " + value, ex); }
    }

    public static CpfRuntimeAckState parse(String value) {
        return require(value);
    }

}
