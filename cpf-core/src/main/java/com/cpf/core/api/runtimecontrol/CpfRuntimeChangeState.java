package com.cpf.core.api.runtimecontrol;

import java.util.Locale;

/** Runtime 변경 정본 상태입니다. DB check constraint와 반드시 동일해야 합니다. */
public enum CpfRuntimeChangeState {
    SCHEDULED,
    APPLYING,
    PARTIAL,
    SUCCESS,
    FAILED,
    CANCELLED,
    EXPIRED,
    ROLLBACK_PENDING,
    ROLLED_BACK,
    SUPERSEDED,
    UNKNOWN_RESULT,
    RECOVERED;

    public static CpfRuntimeChangeState require(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Runtime change state가 필요합니다.");
        try { return valueOf(value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("지원하지 않는 Runtime change state: " + value, ex); }
    }

    public static CpfRuntimeChangeState parse(String value) {
        return require(value);
    }

}
