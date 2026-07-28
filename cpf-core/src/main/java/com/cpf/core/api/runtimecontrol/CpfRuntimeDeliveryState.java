package com.cpf.core.api.runtimecontrol;

import java.util.Locale;

/** Runtime delivery 저장 상태입니다. Agent ACK 입력 상태와 DB 저장 상태의 차이를 명확히 분리합니다. */
public enum CpfRuntimeDeliveryState {
    PENDING,
    CLAIMED,
    ACKED,
    FAILED,
    POISONED,
    UNKNOWN_RESULT,
    RESTART_REQUIRED,
    CANCELLED,
    EXPIRED,
    SUPERSEDED;

    public static CpfRuntimeDeliveryState require(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Runtime delivery state가 필요합니다.");
        try { return valueOf(value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("지원하지 않는 Runtime delivery state: " + value, ex); }
    }

    public static CpfRuntimeDeliveryState parse(String value) {
        return require(value);
    }

}
