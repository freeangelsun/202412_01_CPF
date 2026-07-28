package com.cpf.core.api.runtimecontrol;

import java.util.Locale;

/** Runtime desired/actual 정합성 상태입니다. */
public enum CpfRuntimeDriftState {
    IN_SYNC,
    PENDING,
    DRIFT,
    UNKNOWN,
    UNKNOWN_RESULT,
    PENDING_RESTART,
    EXCLUDED;

    public static CpfRuntimeDriftState require(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Runtime drift state가 필요합니다.");
        try { return valueOf(value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("지원하지 않는 Runtime drift state: " + value, ex); }
    }

    public static CpfRuntimeDriftState parse(String value) {
        return require(value);
    }

}
