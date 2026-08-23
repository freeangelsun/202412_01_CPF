package com.cpf.platform.operations.runtimecontrol;

/** Runtime Consumer가 실제 적용한 결과입니다. */
public record CpfRuntimeApplyResult(
        boolean applied,
        boolean unknownResult,
        boolean restartRequired,
        String actualHash,
        String errorCode,
        String message) {
    private static final int MAX_FAILURE_MESSAGE_LENGTH = 1_000;

    public CpfRuntimeApplyResult {
        if (message != null && message.length() > MAX_FAILURE_MESSAGE_LENGTH) {
            message = message.substring(0, MAX_FAILURE_MESSAGE_LENGTH);
        }
    }

    /** 기존 4-인자 생성자 호환입니다. */
    public CpfRuntimeApplyResult(boolean applied, String actualHash, String errorCode, String message) {
        this(applied, false, false, actualHash, errorCode, message);
    }

    /** 이전 success 중심 Consumer와의 source compatibility입니다. */
    public boolean success() { return applied; }
    public String appliedPayloadHash() { return actualHash; }
    public String failureCode() { return errorCode; }
    public String failureMessage() { return message; }

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
}
