package com.cpf.core.api.runtimecontrol;

/** Runtime Consumer가 실제 적용한 결과입니다. */
public record CpfRuntimeApplyResult(
        boolean applied,
        boolean unknownResult,
        boolean restartRequired,
        String actualHash,
        String errorCode,
        String message) {

    public CpfRuntimeApplyResult {
        int terminalSignals = (applied ? 1 : 0) + (unknownResult ? 1 : 0) + (restartRequired ? 1 : 0);
        if (terminalSignals > 1) {
            throw new IllegalArgumentException(
                    "applied, unknownResult, restartRequired는 동시에 true일 수 없습니다.");
        }
        if ((applied || restartRequired) && (actualHash == null || actualHash.isBlank())) {
            throw new IllegalArgumentException(
                    "적용 성공 또는 재기동 필요 결과에는 actualHash가 필요합니다.");
        }
        actualHash = normalize(actualHash);
        if (actualHash != null && actualHash.length() > 64) {
            throw new IllegalArgumentException("actualHash는 최대 64자입니다.");
        }
        errorCode = normalize(errorCode);
        message = normalize(message);
    }

    /** 기존 4-인자 생성자 호환입니다. */
    public CpfRuntimeApplyResult(boolean applied, String actualHash, String errorCode, String message) {
        this(applied, false, false, actualHash, errorCode, message);
    }

    public static CpfRuntimeApplyResult success(String actualHash) {
        return new CpfRuntimeApplyResult(true, false, false, actualHash, null, null);
    }

    public static CpfRuntimeApplyResult failure(String errorCode, String message) {
        return new CpfRuntimeApplyResult(false, false, false, null, errorCode, message);
    }

    /** Side effect 발생 여부를 판단할 수 없는 결과입니다. 자동 재시도하면 안 됩니다. */
    public static CpfRuntimeApplyResult unknown(String errorCode, String message) {
        return new CpfRuntimeApplyResult(false, true, false, null, errorCode, message);
    }

    /** 변경을 stage했지만 프로세스 재기동이 필요한 결과입니다. */
    public static CpfRuntimeApplyResult restartRequired(String stagedHash, String message) {
        return new CpfRuntimeApplyResult(false, false, true, stagedHash, "RESTART_REQUIRED", message);
    }

    private static String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
